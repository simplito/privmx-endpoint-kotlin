import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.core.Connection
import com.simplito.java.privmx_endpoint.modules.thread.ThreadApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun App() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        LaunchedEffect(Unit){
            coroutineScope {
                withContext(Dispatchers.Default) {
                    launch {
                        try {
                            val privKey = "L41BJTUy5tKbUzowatSwgHQqycFNoF2WUokdjotRDNwdsGMvXNH8"
                            Connection.connect(
                                privKey,
                                "f42b70a7-4e8c-40fa-a05f-f772aced2a1c",
                                "http://192.168.24.152:9111",
                            ).use {
                                val context = it.listContexts(0, 2, sortOrder = "desc").let { pagingList ->
                                    println(pagingList.totalAvailable)

                                    pagingList.readItems?.forEach { context ->
                                        println("My context: ${context}")
                                    }
                                    pagingList.readItems?.first()
                                }
                                context?.let { context ->
                                    ThreadApi(it).use { threadApi->
                                        println("threadApi")
                                        val threadId="67bdbc0f752a0c0fe5a87a65"
//                                        threadApi.deleteThread(threadId)

//                                        val thread = threadApi.getThread(threadId)?.also {
//                                            println(it.threadId + ", " + it.publicMeta?.decodeToString())
//                                        }
                                        threadApi.listThreads(context.contextId,0,10,"desc")?.run {
                                            println(totalAvailable)
                                            readItems?.forEach { item ->
                                                println(item?.threadId)
                                            }
                                        }
                                        threadApi.sendMessage("67bdbcad752a0c0fe5a87a66","public meta".encodeToByteArray(),"private meta".encodeToByteArray(),"message ${Random.nextInt()}".encodeToByteArray())
                                        threadApi.listMessages("67bdbcad752a0c0fe5a87a66",0,10,"desc",null)?.run {
                                            println(totalAvailable)
                                            readItems?.forEach { item ->
                                                println("${item?.data?.decodeToString()}")
                                            }
                                        }
//                                        threadApi.close()
                                    }

//                                    println("TEST")
                                }

                            }
                        }catch (e: PrivmxException){
                            println("error ${e.full}")
                        }
                    }
                }
            }
        }
        Text("Hello", color = Color.Black)
    }
}