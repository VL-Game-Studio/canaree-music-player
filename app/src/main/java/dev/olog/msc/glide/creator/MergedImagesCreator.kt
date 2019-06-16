package dev.olog.msc.glide.creator

import android.content.Context
import android.graphics.Bitmap
import dev.olog.msc.core.MediaId
import dev.olog.msc.utils.assertBackgroundThread
import dev.olog.msc.utils.k.extension.getCachedBitmap
import java.io.File
import java.io.FileOutputStream

internal object MergedImagesCreator {

    fun makeImages(context: Context, albumIdList: List<Long>, parentFolder: String, itemId: String): File? {
        assertBackgroundThread()

        val albumsId = albumIdList.distinctBy { it }
        val uris = mutableListOf<IdWithBitmap>()
        for (id in albumsId) {
            try {
                getBitmap(context, id)?.let { uris.add(IdWithBitmap(id, it)) }
            } catch (ex: Exception){}
            if (uris.size == 9){
                break
            }
        }

        try {
            return doCreate(
                context,
                uris,
                parentFolder,
                itemId
            )
        } catch (ex: OutOfMemoryError) {
            return null
        }
    }

    private fun getBitmap(context: Context, albumId: Long): Bitmap? {
        val bitmap = context.getCachedBitmap(MediaId.albumId(albumId), 500, withError = false)
        return bitmap
    }

    private fun doCreate(context: Context, uris: List<IdWithBitmap>, parentFolder: String, itemId: String): File? {
        val imageDirectory = ImagesFolderUtils.getImageFolderFor(context, parentFolder)

        if (uris.isEmpty()) {
            // new requested image has no childs, delete old if exists
            imageDirectory.listFiles()
                .firstOrNull { it.name.substring(0, it.name.indexOf("_")) == itemId }
                ?.delete()
            return null
        }

        val albumsId = uris.map { it.id }

        // search for old image
        val oldImage = imageDirectory
            .listFiles().firstOrNull { it.name.substring(0, it.name.indexOf("_")) == itemId }

        if (oldImage != null) { // image found
            val fileImageName = oldImage.extractImageName()

            val albumIdsInFilename = fileImageName.containedAlbums()

            val sameImages = albumsId.sorted() == albumIdsInFilename.sorted()
            if (sameImages) {
                // same image, exit
                return oldImage
            } else {
                // images are different
                val progr = fileImageName.progressive()

                // image already exist, create new with a new progr
                oldImage.delete() // first delete old
                return prepareSaveThenSave(
                    uris,
                    imageDirectory,
                    itemId,
                    albumsId,
                    progr + 1L
                )
            }
        } else {
            // create new image
            return prepareSaveThenSave(
                uris,
                imageDirectory,
                itemId,
                albumsId,
                System.currentTimeMillis()
            )
        }
    }

    private fun prepareSaveThenSave(
        uris: List<IdWithBitmap>, directory: File,
        itemId: String,
        albumsId: List<Long>,
        progr: Long
    ): File {

        val bitmap = MergedImageUtils.joinImages(uris.map { it.bitmap })
        val child = ImagesFolderUtils.createFileName(itemId, progr, albumsId)
        return saveFile(directory, child, bitmap)
    }

    private fun saveFile(directory: File, child: String, bitmap: Bitmap): File {
        assertBackgroundThread()

        val dest = File(directory, "$child.webp")
        val out = FileOutputStream(dest)
        bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
        bitmap.recycle()
        out.close()
        return dest
    }

}

data class IdWithBitmap(
    val id: Long,
    val bitmap: Bitmap
)