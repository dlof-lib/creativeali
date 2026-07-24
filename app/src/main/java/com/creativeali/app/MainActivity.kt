package com.creativeali.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.creativeali.app.container.ContainerListScreen
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

/**
 * التبويب الرئيسي الآن قسمان فقط: "حاوية DLoF" التي تجمع المدونة/المذكرة
 * والمخطط/التخطيط داخل كل حاوية (انظر [ContainerListScreen]), ومستكشف DLoF.
 */
@Composable
fun CreativeAliRoot() {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_containers)) }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_dlof)) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> ContainerListScreen()
                else -> DlofExplorerScreen()
            }
        }
    }
}
