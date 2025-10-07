# Reorderableなリストの作成(Android View使用パターン)

## 1. ComposeにViewを埋め込む

### 初期設定

・build.gradle.kts(Module :app)でviewBindingを有効にしておく

```kotlin
    viewBinding{
        enable = true
    }
```

### ①レイアウトの作成(必要に応じて)

リサイクラービューや単体のコンポーネントなど、レイアウトを指定せずに直接呼び出す場合はスキップ可能

今回であれば、リスト全体はデフォルトの`RecyclerView`を使用し、リストのアイテムはXMLを用いてレイアウトを定義する。

・row_list_item.xml

(app→new→Android Resource Fileを選択→Resource typeをレイアウトに変更して作成)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginVertical="4dp"
    android:padding="4dp"
    android:orientation="horizontal">

    <TextView
        android:textSize="16sp"
        android:id="@+id/id_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="TextView" />

    <TextView
        android:textSize="16sp"
        android:layout_marginStart="4dp"
        android:id="@+id/value_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="TextView" />
</LinearLayout>
```

### ②ViewをComposableから呼び出す

`AndroidView`コンポーネントを使用する

```kotlin
AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
       //この中でViewをリターンする
    }
)
```

## RecyclerviewとItemTouchHelper

### ①RecyclerView

`RecyclerView`はLazyColumnのView版コンポーネント

以下の2つの設定が必要

・LayoutManager: リスト表示の方法を指定(Vertical or Horizontal, Grid表示など)

・Adapter: リスト一つ分のアイテムを定義し、データを接続するためのクラス

①`LayoutManager`

今回は単純な縦並びレイアウトのため、LinearLayoutManagerを指定

```kotlin
  recyclerView.layoutManager = LinearLayoutManager(context)
```

②`Adapter`

独自のアダプタークラスを作成

```kotlin
//リサイクラービュー用アダプタークラス
private class MyRecyclerViewAdapter(private val dataList: MutableList<RowItem>) :
    RecyclerView.Adapter<MyRecyclerViewAdapter.MyRecyclerViewViewHolder>() {
        //ViewHolder(一つ分のアイテムのレイアウト)を指定するメソッド
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
    //ViewHolderに一つづつデータを流していくメソッド
    override fun onBindViewHolder(
        holder: MyRecyclerViewViewHolder,
        position: Int
    ) {
        holder.bindData(dataList[position])
    }

    //データの個数を指定するメソッド
    override fun getItemCount(): Int {
        return dataList.size
    }

    //一つ分のアイテムを定義するクラス
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
```

これらをrecyclerViewに取り付けることでリストが表示される

```kotlin
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
        //リサイクラービュークラス
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = MyRecyclerViewAdapter(dataList)
        recyclerView.adapter = adapter
        //Viewクラスを返す
        return@AndroidView recyclerView
    }
)
```

### ②ItemTouchHelper

RecyclerViewにスワイプやドラッグの機能をつけるには、`ItemTouchHelper`を使用する

Adapterなどと同じく、独自のクラスを作成する


```kotlin
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
```

・`ItemTouchHelper.SimpleCallback`クラスをオーバーライドすると作りやすい

この時、一つ目のパラメータがドラッグの受付方向、二つ目のパラメータがスワイプの受付方向

複数方向ある場合は、上記のように`or`をつけて複数指定する

・`onMove`: ドラッグ時の挙動を指定(ドラッグ元のアイテムを削除し、ドラッグ先のポジションに入れ直す)

RecyclerViewに渡したadapterのnotify~メソッドを呼び出すとリストが更新される


・`onSwiped`: スワイプ時の動作を定義(今回は動作なし)

RecyclerViewに取り付ける

```kotlin
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
```

