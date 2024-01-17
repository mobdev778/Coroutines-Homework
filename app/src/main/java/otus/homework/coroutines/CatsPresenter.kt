package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatsPresenter(
    private val catsService: CatsService,
    private val imagesService: ImagesService,
) {

    private var presenterScope: CoroutineScope? = createPresenterScope()
    private var _catsView: ICatsView? = null

    fun onInitComplete() {
        presenterScope?.launch {
            try {
                val fact: Fact
                val images: List<Image>
                withContext(Dispatchers.IO) {
                    val factDeferred = async { catsService.getCatFact() }
                    val imagesDeferred = async { imagesService.getImages() }
                    fact = factDeferred.await()
                    images = imagesDeferred.await()
                }
                _catsView?.populate(
                    CatsUiModel.Post(fact, images.first())
                )
            } catch (ste: java.net.SocketTimeoutException) {
                _catsView?.populate(
                    CatsUiModel.Toast(R.string.http_error_ste)
                )
            } catch (exception: Exception) {
                CrashMonitor.trackWarning()
                _catsView?.populate(
                    CatsUiModel.Toast(R.string.http_error_template, exception.message)
                )
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
        presenterScope = createPresenterScope()
    }

    fun detachView() {
        presenterScope?.cancel()
        _catsView = null
    }

    private fun createPresenterScope(): CoroutineScope {
        presenterScope?.cancel()
        return CoroutineScope(
            Dispatchers.Main + CoroutineName("CatsCoroutine") + SupervisorJob()
        )
    }
}
