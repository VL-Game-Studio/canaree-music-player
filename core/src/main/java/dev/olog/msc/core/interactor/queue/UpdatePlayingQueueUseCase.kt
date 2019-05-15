package dev.olog.msc.core.interactor.queue

import dev.olog.msc.core.MediaId
import dev.olog.msc.core.executors.IoScheduler
import dev.olog.msc.core.gateway.PlayingQueueGateway
import dev.olog.msc.core.interactor.base.CompletableUseCaseWithParam
import io.reactivex.Completable
import javax.inject.Inject

class UpdatePlayingQueueUseCase @Inject constructor(
        schedulers: IoScheduler,
        private val gateway: PlayingQueueGateway

) : CompletableUseCaseWithParam<List<Triple<MediaId, Long, Int>>>(schedulers) {

    override fun buildUseCaseObservable(param: List<Triple<MediaId, Long, Int>>): Completable {
        return gateway.update(param)
    }
}