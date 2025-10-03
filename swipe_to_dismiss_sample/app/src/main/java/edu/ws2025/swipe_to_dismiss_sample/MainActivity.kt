package edu.ws2025.swipe_to_dismiss_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.ws2025.swipe_to_dismiss_sample.ui.theme.Swipe_to_dismiss_sampleTheme
//先にインポートしておく
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Swipe_to_dismiss_sampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SwipeListSample(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

//リスト表示用Composable
@Composable
fun SwipeListSample(modifier: Modifier = Modifier) {
    val testItems = remember {
        mutableStateListOf(
            TodoData(1, "Data1"),
            TodoData(2, "Data2"),
            TodoData(3, "Data3"),
            TodoData(4, "Data4"),
            TodoData(5, "Data5"),
        )
    }
    LazyColumn {
        items(items = testItems, key = {it.id}) {
            DismissListItem(
                item = it,
                //スワイプ時にtestItemsから項目を削除
                onRemove = {item ->
                    testItems -= item
                },
                //アイテム削除時に滑らかに動くよう設定
                modifier = Modifier.animateItem()
            )
        }
    }
}


//ListItem用Composable
@Composable
fun DismissListItem(
    item: TodoData,
    onRemove: (TodoData) -> Unit,
    modifier: Modifier = Modifier
) {
    //スワイプの向きに合わせて動作を定義する
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it ->
           when(it){
               EndToStart -> {
                   onRemove(item)
                   true
               }
               StartToEnd -> false
               Settled -> true
           }
        }
    )

    //リストアイテムのUIコード
    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        //左から右のスワイプを無効化
        enableDismissFromStartToEnd = false,
        modifier = Modifier.fillMaxSize(),
        backgroundContent = {
            //スワイプした時の背景のUIを定義
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
            )
        }
    ) {
        //リストアイテム本体のUI(任意のComposable)
        ListItem(
            headlineContent = { Text(item.id.toString()) },
            supportingContent = { Text(item.text) }
        )
    }
}

