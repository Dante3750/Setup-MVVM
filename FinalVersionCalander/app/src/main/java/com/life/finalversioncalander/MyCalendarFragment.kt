package com.life.finalversioncalander

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.life.finalversioncalander.databinding.CalendarFragmentBinding
import com.life.finalversioncalander.databinding.CalendarDayBinding
import com.life.finalversioncalander.databinding.CalendarHeaderBinding
import com.life.finalversioncalander.databinding.EventItemViewBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

data class Event(val id: String, val text: String, val date: LocalDate)

class MyCalendarEventsAdapter(val onClick: (Event) -> Unit) :
        RecyclerView.Adapter<MyCalendarEventsAdapter.MyCalendarEventsViewHolder>() {

    val events = mutableListOf<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCalendarEventsViewHolder {
        return MyCalendarEventsViewHolder(
                EventItemViewBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: MyCalendarEventsViewHolder, position: Int) {
        viewHolder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class MyCalendarEventsViewHolder(private val binding: EventItemViewBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onClick(events[bindingAdapterPosition])
            }
        }

        fun bind(event: Event) {
            binding.itemEventText.text = event.text
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class MyCalendarFragment : BaseFragment(R.layout.calendar_fragment), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val eventsAdapter = MyCalendarEventsAdapter {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.example_3_dialog_delete_confirmation)
                .setPositiveButton(R.string.delete) { _, _ ->
                    deleteEvent(it)
                }
                .setNegativeButton(R.string.close, null)
                .show()
    }

    private val inputDialog by lazy {
        val editText = AppCompatEditText(requireContext())
        val layout = FrameLayout(requireContext()).apply {
            // Setting the padding on the EditText only pads the input area
            // not the entire EditText so we wrap it in a FrameLayout.
            val padding = dpToPx(20, requireContext())
            setPadding(padding, padding, padding, padding)
            addView(editText, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.example_3_input_dialog_title))
                .setView(layout)
                .setPositiveButton(R.string.save) { _, _ ->
                    saveEvent(editText.text.toString())
                    // Prepare EditText for reuse.
                    editText.setText("")
                }
                .setNegativeButton(R.string.close, null)
                .create()
                .apply {
                    setOnShowListener {
                        // Show the keyboard
                        editText.requestFocus()
                        context.inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    }
                    setOnDismissListener {
                        // Hide the keyboard
                        context.inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                    }
                }
    }

    override val titleRes: Int = R.string.example_3_title

    private var selectedDate: LocalDate? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val today = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")

    @RequiresApi(Build.VERSION_CODES.O)
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val events = mutableMapOf<LocalDate, List<Event>>()

    private lateinit var binding: CalendarFragmentBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CalendarFragmentBinding.bind(view)
        binding.exRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        binding.exCalendar.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null) {
            binding.exCalendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }
        binding.exCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.exDayText
                val dotView = container.binding.exDotView

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        today -> {
                            textView.setTextColorRes(R.color.example_3_white)
                            textView.setBackgroundResource(R.drawable.example_3_today_bg)
                            dotView.makeInVisible()
                        }
                        selectedDate -> {
                            textView.setTextColorRes(R.color.example_3_blue)
                            textView.setBackgroundResource(R.drawable.example_3_selected_bg)
                            dotView.makeInVisible()
                        }
                        else -> {
                            textView.setTextColorRes(R.color.example_3_black)
                            textView.background = null
                            dotView.isVisible = events[day.date].orEmpty().isNotEmpty()
                        }
                    }
                } else {
                    textView.makeInVisible()
                    dotView.makeInVisible()
                }
            }
        }



        binding.exNextMonthImage.setOnClickListener {
            binding.exCalendar.findFirstVisibleMonth()?.let {
                binding.exCalendar.smoothScrollToMonth(it.yearMonth.next)
            }
        }

        binding.exPreviousMonthImage.setOnClickListener {
            binding.exCalendar.findFirstVisibleMonth()?.let {
                binding.exCalendar.smoothScrollToMonth(it.yearMonth.previous)
            }
        }


        binding.exCalendar.monthScrollListener = { month ->
            val title = "${titleSameYearFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            binding.exMonthYearText.text = title

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.exCalendar.notifyDateChanged(it)
            }
        }
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }
        binding.exCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
                        tv.setTextColorRes(R.color.example_3_black)
                    }
                }
            }
        }

        binding.exAddButton.setOnClickListener {
            inputDialog.show()
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.exCalendar.notifyDateChanged(it) }
            binding.exCalendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveEvent(text: String) {
        if (text.isBlank()) {
            Toast.makeText(requireContext(), R.string.example_3_empty_input_text, Toast.LENGTH_LONG).show()
        } else {
            selectedDate?.let {
                events[it] = events[it].orEmpty().plus(Event(UUID.randomUUID().toString(), text, it))
                updateAdapterForDate(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteEvent(event: Event) {
        val date = event.date
        events[date] = events[date].orEmpty().minus(event)
        updateAdapterForDate(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapterForDate(date: LocalDate) {
        eventsAdapter.apply {
            events.clear()
            events.addAll(this@MyCalendarFragment.events[date].orEmpty())
            notifyDataSetChanged()
        }
        binding.exSelectedDateText.text = selectionFormatter.format(date)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.example_5_toolbar_color)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
    }
}
