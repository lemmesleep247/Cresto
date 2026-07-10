package com.nevoit.cresto.feature.recentlydeleted

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import com.nevoit.cresto.data.todo.TodoItemWithSubTodos
import com.nevoit.cresto.theme.AppColors
import com.nevoit.cresto.theme.AppSpecs
import com.nevoit.cresto.ui.components.glasense.SwipeableListState
import com.nevoit.cresto.ui.components.packed.SwipeableRecentlyDeletedTodoItem
import com.nevoit.glasense.core.interaction.DimIndication
import com.nevoit.glasense.theme.tokens.Springs

@Composable
fun LazyItemScope.RecentlyDeletedTodoListItemRow(
    item: TodoItemWithSubTodos,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    overlayInteractionSource: MutableInteractionSource,
    swipeListState: SwipeableListState,
    onEnterSelection: () -> Unit,
    onToggleSelection: () -> Unit,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val selectionAlpha = remember { Animatable(if (isSelected) 1f else 0f) }
    val rowInteractionSource = remember { MutableInteractionSource() }
    val selectionOutline = AppColors.primary
    val cardCorner = AppSpecs.cardCorner

    LaunchedEffect(isSelected) {
        selectionAlpha.animateTo(if (isSelected) 1f else 0f, tween(100))
    }

    Box(
        modifier = Modifier
            .animateItem(placementSpec = Springs.crisp())
            .combinedClickable(
                interactionSource = rowInteractionSource,
                indication = DimIndication(shape = AppSpecs.cardShape),
                onLongClick = {
                    if (isSelectionModeActive) onToggleSelection() else onEnterSelection()
                },
                onClick = {
                    if (isSelectionModeActive) onToggleSelection()
                }
            )
    ) {
        SwipeableRecentlyDeletedTodoItem(
            listState = swipeListState,
            item = item,
            showDate = true,
            isDueTodayMarkerEnabled = false,
            isOverdueMarkerEnabled = false,
            onRestore = onRestore,
            onDeletePermanently = onDeletePermanently,
            modifier = Modifier.drawBehind {
                if (selectionAlpha.value > 0f) {
                    val outline = RoundedRectangle(cardCorner - 3.dp / 2).createOutline(
                        size = androidx.compose.ui.geometry.Size(
                            size.width - 3.dp.toPx(),
                            size.height - 3.dp.toPx()
                        ),
                        layoutDirection = LayoutDirection.Ltr,
                        density = this
                    )
                    translate(1.5.dp.toPx(), 1.5.dp.toPx()) {
                        drawOutline(
                            outline = outline,
                            color = selectionOutline,
                            alpha = selectionAlpha.value,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
        )

        if (isSelectionModeActive) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .combinedClickable(
                        interactionSource = overlayInteractionSource,
                        indication = null,
                        onClick = onToggleSelection
                    )
            )
        }
    }
}
