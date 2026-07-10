package com.nevoit.cresto.feature.recentlydeleted

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.nevoit.cresto.R
import com.nevoit.cresto.data.todo.TodoViewModel
import com.nevoit.cresto.theme.AppButtonColors
import com.nevoit.cresto.theme.AppColors
import com.nevoit.cresto.ui.components.glasense.DialogItemData
import com.nevoit.cresto.ui.components.glasense.DialogState
import com.nevoit.cresto.ui.components.glasense.GlasenseButtonToolBar
import com.nevoit.cresto.ui.components.glasense.GlasenseDialog
import com.nevoit.cresto.ui.components.glasense.GlasenseDynamicSmallTitle
import com.nevoit.cresto.ui.components.glasense.isScrolledPast
import com.nevoit.cresto.ui.components.glasense.rememberSwipeableListState
import com.nevoit.cresto.ui.components.packed.PageContent
import com.nevoit.cresto.ui.components.packed.SwipeableRecentlyDeletedTodoItem
import com.nevoit.glasense.component.paddingItem
import com.nevoit.glasense.core.component.Icon
import com.nevoit.glasense.core.component.Text
import com.nevoit.glasense.core.component.VGap
import com.nevoit.glasense.theme.GlasenseTheme

@Composable
fun RecentlyDeletedScreen(viewModel: TodoViewModel) {
    val activity = LocalActivity.current
    val todos by viewModel.recentlyDeletedTodos.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val swipeListState = rememberSwipeableListState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val isSmallTitleVisible by listState.isScrolledPast(12.dp)
    val backgroundColor = AppColors.pageBackground
    val backdrop = rememberLayerBackdrop {
        drawRect(
            color = backgroundColor,
            size = Size(this.size.width * 3, this.size.height * 3),
            topLeft = Offset(-this.size.width, -this.size.height)
        )
        drawContent()
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            swipeListState.close()
        }
    }

    var dialogState by remember { mutableStateOf(DialogState()) }
    val cancelText = stringResource(R.string.cancel)
    val deletePermanentlyText = stringResource(R.string.delete_permanently)
    val deletePermanentlyTitle = stringResource(R.string.delete_permanently_title)
    val deletePermanentlyMessage = stringResource(R.string.delete_permanently_message)
    val deleteIcon = painterResource(R.drawable.ic_trash)
    val requestPermanentDelete: (Int) -> Unit = { todoId ->
        dialogState = DialogState(
            isVisible = true,
            items = listOf(
                DialogItemData(
                    text = cancelText,
                    onClick = {},
                    isPrimary = false
                ),
                DialogItemData(
                    text = deletePermanentlyText,
                    icon = deleteIcon,
                    onClick = { viewModel.deletePermanentlyById(todoId) },
                    isPrimary = true,
                    isDestructive = true
                )
            ),
            title = deletePermanentlyTitle,
            message = deletePermanentlyMessage
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        PageContent(
            state = listState,
            modifier = Modifier.layerBackdrop(backdrop),
            tabPadding = false
        ) {
            item(key = "header") {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(60.dp)
                )
            }
            if (todos.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .padding(top = 48.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.no_recently_deleted_artwork),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(96.dp)
                                    .scale(1.2f),
                                colorFilter = ColorFilter.tint(AppColors.primary)
                            )
                            Text(
                                text = stringResource(id = R.string.recently_deleted_empty),
                                color = AppColors.content,
                                modifier = Modifier.padding(top = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }

                    }
                }
            } else {
                itemsIndexed(
                    items = todos,
                    key = { _, item -> item.todoItem.id }
                ) { index, item ->
                    SwipeableRecentlyDeletedTodoItem(
                        listState = swipeListState,
                        item = item,
                        showDate = true,
                        isDueTodayMarkerEnabled = false,
                        isOverdueMarkerEnabled = false,
                        onRestore = { viewModel.restoreById(item.todoItem.id) },
                        onDeletePermanently = {
                            requestPermanentDelete(item.todoItem.id)
                        }
                    )
                    VGap()
                }
            }
            paddingItem(listState)
        }

        GlasenseDynamicSmallTitle(
            modifier = Modifier.align(Alignment.TopCenter),
            statusBarHeight = statusBarHeight,
            isVisible = isSmallTitleVisible,
            backdrop = backdrop
        ) {
            Text(
                stringResource(R.string.recently_deleted),
                style = GlasenseTheme.type.headline,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.Center),
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp)
        ) {
            GlasenseButtonToolBar(
                enabled = true,
                shape = CircleShape,
                onClick = { activity?.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(48.dp),
                colors = AppButtonColors.action(),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward_nav),
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.width(32.dp)
                )
            }
        }

        GlasenseDialog(
            dialogState = dialogState,
            backdrop = backdrop,
            onDismiss = { dialogState = dialogState.copy(isVisible = false) }
        )
    }
}
