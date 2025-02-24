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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                            Connection.connect(
                                "L41BJTUy5tKbUzowatSwgHQqycFNoF2WUokdjotRDNwdsGMvXNH8",
                                "f42b70a7-4e8c-40fa-a05f-f772aced2a1c",
                                "http://192.168.24.187:9111",
                            ).use {
                                it.listContexts(0, 2, sortOrder = "desc").let { pagingList ->
                                    println(pagingList.totalAvailable)
                                    pagingList.readItems?.forEach { context ->
                                        println("My context: ${context}")
                                    }
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