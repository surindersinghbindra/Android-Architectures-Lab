package com.ekobits.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekobits.demoapp.presentation.mvvm.UserProfileMvvmScreen
import com.ekobits.demoapp.ui.theme.DemoAppTheme
import dagger.hilt.android.AndroidEntryPoint


data class Category(val id: String, val name: String)

data class Product(val id: String, val name: String, val price: String)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        UserProfileMvvmScreen(
                            userId = "8872415154",
                            viewModel = hiltViewModel()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProductCatalogScreen(
    categories: List<Category>, products: List<Product>, onProductClick: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Discover our products",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // 2. Horizontal Category Bar inside the Vertical Feed
        item {
            CategoryFilterBar(categories = categories)
        }

        // 3. Dynamic List of Products
        items(items = products, key = { product -> product.id } // ⚠️ IMPORTANT for performance!
        ) { product ->
            ProductItemCard(
                product = product, onClick = { onProductClick(product) })
        }

    }


}

// Horizontal Category Row
@Composable
fun CategoryFilterBar(categories: List<Category>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = categories, key = { category -> category.id }) { category ->
            SuggestionChip(onClick = { /* Filter category */ }, label = { Text(category.name) })
        }
    }
}

// Single Product Item Representation
@Composable
fun ProductItemCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = product.price, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View details"
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Preview(
    showBackground = false, name = "prev",
)
@Composable
fun GreetingPreview() {
    DemoAppTheme {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(30.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(
                    horizontal = 16.dp, vertical = 8.dp
                )
        ) {
            Greeting("Android")
            Greeting("Android")
        }
    }
}

private val sampleCategories = listOf(
    Category("1", "Electronics"),
    Category("2", "Clothing"),
    Category("3", "Home"),
    Category("4", "Beauty"),
    Category("5", "Books")
)

private val sampleProducts = listOf(
    Product("1", "Smartphone", "$999"),
    Product("2", "Wireless Headphones", "$199"),
    Product("3", "Running Shoes", "$89"),
    Product("4", "Coffee Maker", "$49"),
    Product("5", "Backpack", "$39"),
    Product("6", "Smart Watch", "$299"),
    Product("7", "Yoga Mat", "$29")
)

@Preview(showBackground = true)
@Composable
fun ProductCatalogScreenPreview() {
    DemoAppTheme {
        ProductCatalogScreen(
            categories = sampleCategories, products = sampleProducts, onProductClick = {})
    }
}

