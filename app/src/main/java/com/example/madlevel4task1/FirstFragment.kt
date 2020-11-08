package com.example.madlevel4task1

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlevel4task1.model.Product
import com.example.madlevel4task1.repository.ProductRepository
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.android.synthetic.main.fragment_first.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    // Room
    private lateinit var productRepository: ProductRepository
    private val mainScope = CoroutineScope(Dispatchers.Main)

    // Recyclerview
    private var products: ArrayList<Product> = arrayListOf<Product>()
    private val productAdapter = ProductAdapter(products)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Repository needs context
        productRepository = ProductRepository(requireContext())
        // Fetch database
        getShoppingListFromDatabase()

        initViews()

        fabAdd.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun initViews() {
        rvProducts.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvProducts.adapter = productAdapter
        rvProducts.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    private fun createItemTouchHelper(): ItemTouchHelper {

        // Swipe left to delete product
        val callback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                // Adapter has index the Repository needs
                val position = viewHolder.adapterPosition
                val productToDelete = products[position]

                // In IO coroutine
                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        productRepository.deleteProduct(productToDelete)
                    }
                    getShoppingListFromDatabase()
                }
            }
        }
        return ItemTouchHelper(callback)
    }

    @SuppressLint
    private fun showAddProductDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.dialog_add_product))
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_add_product, null)

        val productName = dialogLayout.findViewById<EditText>(R.id.txt_product_name)
        val amount = dialogLayout.findViewById<EditText>(R.id.txt_amount)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.dialog_ok_btn) { _: DialogInterface, _: Int ->
            addProduct(productName, amount)
        }
        builder.show()
    }

    private fun addProduct(txtProductName: EditText, txtAmount: EditText) {
        if (validateFields(txtProductName, txtAmount)) {
            mainScope.launch {
                val product = Product(
                    productName = txtProductName.text.toString(),
                    productQuantity = txtAmount.text.toString().toInt()
                )

                withContext(Dispatchers.IO) {
                    productRepository.insertProduct(product)
                }

                getShoppingListFromDatabase()
            }
        }
    }

    private fun validateFields(txtProductName: EditText , txtAmount: EditText): Boolean {
        return if (txtProductName.text.toString().isNotBlank()
            && txtAmount.text.toString().isNotBlank()
        ) {
            true
        } else {
            Toast.makeText(activity, "Please fill in the fields", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun getShoppingListFromDatabase() {
        mainScope.launch {
            // Moves data handling from the main (UI) thread to the IO thread
            val products = withContext(Dispatchers.IO) {
                productRepository.getAllProducts()
            }
            this@FirstFragment.products.clear()
            this@FirstFragment.products.addAll(products)
            this@FirstFragment.productAdapter.notifyDataSetChanged()
        }
    }
}