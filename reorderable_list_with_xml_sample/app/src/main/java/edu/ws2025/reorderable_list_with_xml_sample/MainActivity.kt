package edu.ws2025.reorderable_list_with_xml_sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ws2025.reorderable_list_with_xml_sample.databinding.RowListItemBinding
import edu.ws2025.reorderable_list_with_xml_sample.ui.theme.Reorderable_list_with_xml_sampleTheme




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Reorderable_list_with_xml_sampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReorderableListView(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


//XMLレイアウトを含むReorderableなリスト
@Composable
fun ReorderableListView(modifier: Modifier = Modifier) {
    val dataList = remember {
        mutableStateListOf<RowItem>().apply {
            addAll((0 until 50).map { RowItem(it, "Item #${it}") })
        }
    }
    //AndroidView内でView(XML)を呼び出せる
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            //リサイクラービュークラス
            val recyclerView = RecyclerView(context)
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = MyRecyclerViewAdapter(dataList)
            recyclerView.adapter = adapter
            //ドラッグ(スワイプ)用のItemToucheHelper呼び出し
            val itemTouchHelper = ItemTouchHelper(ComposeItemTouchHelper(dataList, adapter))
            //リサイクラービューに取り付け
            itemTouchHelper.attachToRecyclerView(recyclerView)
            //Viewクラスを返す
            return@AndroidView recyclerView
        }
    )


}
//リストドラッグ時の挙動用ItemTouchHelperクラス
private class ComposeItemTouchHelper(
    val dataList: MutableList<RowItem>,
    val adapter: MyRecyclerViewAdapter
) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
    //ドラッグ時の動作定義
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val data = dataList.removeAt(viewHolder.layoutPosition)
        dataList.add(target.layoutPosition, data)
        adapter.notifyItemMoved(viewHolder.layoutPosition, target.layoutPosition)
        println(dataList)
        return true
    }

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int
    ) {
    }
}

//リサイクラービュー用アダプタークラス
private class MyRecyclerViewAdapter(private val dataList: MutableList<RowItem>) :
    RecyclerView.Adapter<MyRecyclerViewAdapter.MyRecyclerViewViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyRecyclerViewViewHolder {
        return MyRecyclerViewViewHolder(
            RowListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: MyRecyclerViewViewHolder,
        position: Int
    ) {
        holder.bindData(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class MyRecyclerViewViewHolder(private val b: RowListItemBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bindData(data: RowItem) {
            b.apply {
                idText.text = data.id.toString()
                valueText.text = data.text
            }
        }
    }
}