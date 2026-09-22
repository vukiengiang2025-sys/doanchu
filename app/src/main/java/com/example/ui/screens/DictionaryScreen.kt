package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.DictionaryWord
import com.example.dictionary.DictionaryRepository
import com.example.dictionary.DictionaryValidator
import com.example.importexport.DictionaryExporter
import com.example.importexport.DictionaryImporter
import com.example.importexport.ImportReport
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    repository: DictionaryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    val wordsList by (if (searchQuery.isBlank()) repository.allWordsFlow else repository.search(searchQuery))
        .collectAsState(initial = emptyList())

    val totalCount by repository.totalCountFlow.collectAsState(initial = 0)
    val activeCount by repository.activeCountFlow.collectAsState(initial = 0)
    val userAddedCount by repository.userAddedCountFlow.collectAsState(initial = 0)

    var showAddDialog by remember { mutableStateOf(false) }
    var importReport by remember { mutableStateOf<ImportReport?>(null) }
    var selectedExportFormat by remember { mutableStateOf<String?>(null) }

    // SAF Import Launcher
    var pendingImportFormat by remember { mutableStateOf("TXT") }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val report = DictionaryImporter.importFromUri(
                    context = context,
                    uri = uri,
                    format = pendingImportFormat,
                    dictionaryRepository = repository
                )
                importReport = report
            }
        }
    }

    // SAF Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null && selectedExportFormat != null) {
            scope.launch {
                val count = DictionaryExporter.exportToUri(
                    context = context,
                    uri = uri,
                    format = selectedExportFormat!!
                )
                snackbarHostState.showSnackbar("Đã xuất $count từ thành công!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Từ điển", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_dict")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Force reload bundled dictionary button
                    IconButton(onClick = {
                        scope.launch {
                            val count = repository.reloadBundledDictionary()
                            snackbarHostState.showSnackbar("Đã đồng bộ lại $count từ tiếng Việt từ tệp từ điển offline!")
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Nạp lại từ điển gốc")
                    }
                    // Import menu button
                    IconButton(onClick = {
                        pendingImportFormat = "TXT"
                        importLauncher.launch(arrayOf("text/plain", "text/csv", "application/json"))
                    }) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = "Nhập từ")
                    }
                    // Export button
                    IconButton(onClick = {
                        selectedExportFormat = "TXT"
                        exportLauncher.launch("tu_dien_tieng_viet.txt")
                    }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Xuất từ")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_add_word")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm từ mới")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats summary card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    StatBadge("Tổng từ", totalCount.toString())
                    StatBadge("Đang bật", activeCount.toString())
                    StatBadge("Người dùng thêm", userAddedCount.toString())
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm kiếm từ trong từ điển...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Words List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(wordsList, key = { it.id }) { word ->
                    WordItemCard(
                        word = word,
                        onToggle = { enabled ->
                            scope.launch { repository.toggleWordEnabled(word.id, enabled) }
                        },
                        onDelete = {
                            scope.launch { repository.deleteWord(word.id) }
                        }
                    )
                }
            }
        }
    }

    // Add Word Dialog
    if (showAddDialog) {
        AddWordDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { text, type, freq ->
                scope.launch {
                    val result = repository.addWord(text, type, freq)
                    if (result.isSuccess) {
                        showAddDialog = false
                        snackbarHostState.showSnackbar("Đã thêm từ \"$text\" thành công!")
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Lỗi thêm từ")
                    }
                }
            }
        )
    }

    // Import Report Dialog
    importReport?.let { report ->
        AlertDialog(
            onDismissRequest = { importReport = null },
            confirmButton = {
                Button(onClick = { importReport = null }) {
                    Text("Đóng")
                }
            },
            title = { Text("Kết quả nhập dữ liệu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Đã đọc: ${report.totalLinesRead} dòng")
                    Text("• Từ hợp lệ đã thêm: ${report.validWordsCount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("• Từ trùng bỏ qua: ${report.duplicateWordsCount}")
                    Text("• Từ lỗi: ${report.errorCount}", color = if (report.errorCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)

                    if (report.sampleErrors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Một số lỗi mẫu:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        report.sampleErrors.forEach { err ->
                            Text("  - $err", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WordItemCard(
    word: DictionaryWord,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (word.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val isCompound = word.text.contains(' ')
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isCompound)
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCompound) "Từ ghép • ${word.length} ô" else "Từ đơn • ${word.length} ô",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompound) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Loại: ${word.wordType} • Tần suất: ${word.frequency} • Nguồn: ${word.source}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = word.enabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa từ",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddWordDialog(
    onDismiss: () -> Unit,
    onAdd: (text: String, type: String, freq: Int) -> Unit
) {
    var wordText by remember { mutableStateOf("") }
    var wordType by remember { mutableStateOf("noun") }
    var frequencyText by remember { mutableStateOf("50") }

    val validation = remember(wordText) {
        if (wordText.isBlank()) null else DictionaryValidator.validate(wordText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm từ mới vào từ điển", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = wordText,
                    onValueChange = { wordText = it },
                    label = { Text("Từ tiếng Việt") },
                    singleLine = true,
                    isError = validation?.isValid == false,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validation?.isValid == false) {
                    Text(
                        text = validation.errorMessage ?: "Từ không hợp lệ",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = wordType,
                    onValueChange = { wordType = it },
                    label = { Text("Loại từ (noun, verb, adj...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = frequencyText,
                    onValueChange = { frequencyText = it },
                    label = { Text("Tần suất xuất hiện (1 - 100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val freq = frequencyText.toIntOrNull() ?: 50
                    onAdd(wordText.trim(), wordType.trim(), freq)
                },
                enabled = validation?.isValid == true
            ) {
                Text("Lưu từ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
