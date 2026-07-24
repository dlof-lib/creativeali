package com.creativeali.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.creativeali.app.blogging.BloggingScreen
import com.creativeali.app.diagrams.DiagramScreen
import com.creativeali.app.dlof.ui.DlofExplorerScreen
import com.creativeali.app.ui.theme.CreativeAliTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreativeAliTheme {
                CreativeAliRoot()
            }
        }
    }
}

@Composable
fun CreativeAliRoot() {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_blogging)) }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_diagrams)) }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_dlof)) }
                )
            }
        }
    ) { padding ->
        Modifier.padding(padding) // reserved for shared padding if content needs it
        when (tab) {
            0 -> BloggingScreen()
            1 -> DiagramScreen()
            else -> DlofExplorerScreen()
        }
    }
}
