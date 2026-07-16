package com.nevoit.cresto.feature.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.nevoit.cresto.MainActivity
import com.nevoit.cresto.R
import com.nevoit.cresto.data.todo.EXTRA_TODO_ID
import com.nevoit.cresto.data.todo.TodoItem
import com.nevoit.cresto.data.todo.TodoItemWithSubTodos
import com.nevoit.cresto.data.todo.TodoRepository
import com.nevoit.cresto.feature.detail.DetailActivity
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext
import java.time.LocalDate
import java.time.LocalDateTime

private val WidgetBackground = DayNightColorProvider(
    day = Color(0xFFF4F4F2),
    night = Color(0xFF242422)
)
private val WidgetContent = DayNightColorProvider(
    day = Color(0xFF1B1B19),
    night = Color(0xFFF3F3EF)
)
private val WidgetSecondary = DayNightColorProvider(
    day = Color(0xFF6D6D67),
    night = Color(0xFFB7B7AE)
)

private val TodoIdKey = ActionParameters.Key<Int>(EXTRA_TODO_ID)

private data class TodayTodoWidgetState(
    val todos: List<TodoItem>,
    val remainingCount: Int
)

/** A compact view of the incomplete todos due today. */
object TodayTodoWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(250.dp, 180.dp),
            DpSize(250.dp, 250.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = GlobalContext.get().get<TodoRepository>()
        val todayTodos = repository.getTodosByDate(LocalDate.now())
        val initialTodos = todayTodos.first()

        provideContent {
            val todos by todayTodos.collectAsState(initial = initialTodos)
            TodayTodoWidgetContent(context = context, state = todos.toWidgetState())
        }
    }
}

private const val MAX_VISIBLE_TODOS = 5

private fun List<TodoItemWithSubTodos>.toWidgetState(): TodayTodoWidgetState {
    val todos = asSequence()
        .map { it.todoItem }
        .filterNot { it.isCompleted }
        .sortedWith(compareByDescending<TodoItem> { it.isPinned }
            .thenByDescending { it.creationDateTime })
        .toList()
    return TodayTodoWidgetState(
        todos = todos.take(MAX_VISIBLE_TODOS),
        remainingCount = todos.size
    )
}

private fun textStyle(
    color: ColorProvider = WidgetContent,
    size: Int = 14,
    weight: FontWeight? = null
) = TextStyle(color = color, fontSize = size.sp, fontWeight = weight)

@androidx.compose.runtime.Composable
private fun TodayTodoWidgetContent(
    context: Context,
    state: TodayTodoWidgetState
) {
    val visibleTodoCount = when {
        LocalSize.current.height < 160.dp -> 1
        LocalSize.current.height < 220.dp -> 3
        else -> MAX_VISIBLE_TODOS
    }
    val visibleTodos = state.todos.take(visibleTodoCount)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .cornerRadius(20.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.today),
                    style = textStyle(size = 17, weight = FontWeight.Bold)
                )
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.today_widget_remaining,
                        state.remainingCount,
                        state.remainingCount
                    ),
                    style = textStyle(color = WidgetSecondary, size = 12)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        if (visibleTodos.isEmpty()) {
            Text(
                text = context.getString(R.string.today_widget_empty),
                style = textStyle(color = WidgetSecondary, size = 13)
            )
        } else {
            visibleTodos.forEach { todo ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(
                            actionStartActivity<DetailActivity>(
                                actionParametersOf(TodoIdKey to todo.id)
                            )
                        )
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    CheckBox(
                        checked = false,
                        onCheckedChange = actionRunCallback<CompleteTodayTodoAction>(
                            actionParametersOf(TodoIdKey to todo.id)
                        ),
                        modifier = GlanceModifier.size(28.dp),
                        text = ""
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = todo.title,
                        style = textStyle(size = 14),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}

class CompleteTodayTodoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val todoId = parameters[TodoIdKey] ?: return
        GlobalContext.get().get<TodoRepository>().updateCompletedStatusByIds(
            ids = listOf(todoId),
            isCompleted = true,
            completedDateTime = LocalDateTime.now()
        )
        TodayTodoWidget.update(context, glanceId)
    }
}
