package com.dawnbellsuha.uha.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dawnbellsuha.uha.databinding.FragmentResourcesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import com.dawnbellsuha.uha.R

class ResourcesFragment : Fragment() {

    private var _binding: FragmentResourcesBinding? = null
    private val binding get() = _binding!!

    // Firestore Database Reference
    private lateinit var firestore: FirebaseFirestore
    private val listeners = mutableListOf<ListenerRegistration>()

    // Data Models
    private var careerGuidance: CareerGuidanceModel? = null
    private var todayInHistory: HistoryModel? = null
    private var specialEvent: SpecialEventModel? = null
    private var didYouKnowFact: FactModel? = null
    private var visualContent: VisualContentModel? = null
    private var expertQuote: QuoteModel? = null

    private var allFacts: List<FactModel> = emptyList()
    private var allQuotes: List<QuoteModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFirestore()
        setupClickListeners()
        setupCurrentDate()
        loadDataFromFirestore()
    }

    private fun initializeFirestore() {
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        binding.tvHistoryDate.text = dateFormat.format(Date())
    }

    private fun setupClickListeners() {
        with(binding) {
            btnReadMoreCareer.setOnClickListener {
                careerGuidance?.readMoreUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        openUrlInBrowser(url)
                    }
                }
            }

            btnRefreshFact.setOnClickListener {
                loadRandomFact()
            }

            // Long click listeners for sharing
            ivEducationalVisual.setOnLongClickListener {
                shareVisualContent()
                true
            }

            tvExpertQuote.setOnLongClickListener {
                shareQuote()
                true
            }

            tvDidYouKnow.setOnLongClickListener {
                shareFact()
                true
            }
        }
    }

    private fun loadDataFromFirestore() {
        loadCareerGuidance()
        loadTodayInHistory()
        loadSpecialEvents()
        loadDidYouKnowFacts()
        loadVisualContent()
        loadExpertQuotes()
    }

    private fun loadCareerGuidance() {
        val listener = firestore.collection("educational_resources")
            .document("career_guidance")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading career guidance", error)
                    showError("Failed to load career guidance")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        careerGuidance = document.toObject(CareerGuidanceModel::class.java)
                        careerGuidance?.let {
                            binding.tvCareerGuidance.text = it.content
                        }
                    }
                }
            }
        listeners.add(listener)
    }

    private fun loadTodayInHistory() {
        val todayKey = getCurrentDateKey()
        val listener = firestore.collection("educational_resources")
            .document("today_in_history")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading history", error)
                    showError("Failed to load history content")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        val historyData = document.data?.get(todayKey) as? Map<String, Any>
                        historyData?.let { data ->
                            todayInHistory = HistoryModel(
                                content = data["content"] as? String ?: "",
                                featuredYear = data["featuredYear"] as? String ?: "",
                                imageUrl = data["imageUrl"] as? String ?: "",
                                category = data["category"] as? String ?: ""
                            )

                            todayInHistory?.let { history ->
                                with(binding) {
                                    tvHistoryContent.text = history.content
                                    tvHistoryYear.text = "Featured Year: ${history.featuredYear}"
                                }
                            }
                        }
                    }
                }
            }
        listeners.add(listener)
    }

    private fun loadSpecialEvents() {
        val todayKey = getCurrentDateKey()
        val listener = firestore.collection("educational_resources")
            .document("special_events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading special events", error)
                    showError("Failed to load special events")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        val eventData = document.data?.get(todayKey) as? Map<String, Any>
                        eventData?.let { data ->
                            specialEvent = SpecialEventModel(
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                emoji = data["emoji"] as? String ?: "",
                                category = data["category"] as? String ?: ""
                            )

                            specialEvent?.let { event ->
                                with(binding) {
                                    tvSpecialEventTitle.text = event.title
                                    tvSpecialEventDescription.text = event.description
                                }
                            }
                        }
                    }
                }
            }
        listeners.add(listener)
    }

    private fun loadDidYouKnowFacts() {
        val listener = firestore.collection("educational_resources")
            .document("did_you_know_facts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading facts", error)
                    showError("Failed to load facts")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        val factsData = document.data?.get("facts") as? List<Map<String, Any>>
                        factsData?.let { factsList ->
                            allFacts = factsList.map { factMap ->
                                FactModel(
                                    id = factMap["id"] as? String ?: "",
                                    content = factMap["content"] as? String ?: "",
                                    category = factMap["category"] as? String ?: "",
                                    difficulty = factMap["difficulty"] as? String ?: ""
                                )
                            }

                            if (allFacts.isNotEmpty()) {
                                didYouKnowFact = allFacts[Random.nextInt(allFacts.size)]
                                didYouKnowFact?.let {
                                    binding.tvDidYouKnow.text = it.content
                                }
                            }
                        }
                    }
                }
            }
        listeners.add(listener)
    }

    private fun loadRandomFact() {
        if (allFacts.isNotEmpty()) {
            didYouKnowFact = allFacts[Random.nextInt(allFacts.size)]
            didYouKnowFact?.let {
                binding.tvDidYouKnow.text = it.content
            }
        }
    }

    private fun loadVisualContent() {
        val todayKey = getCurrentDateKey()
        val listener = firestore.collection("educational_resources")
            .document("visual_content")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading visual content", error)
                    showError("Failed to load visual content")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        val visualData = document.data?.get(todayKey) as? Map<String, Any>
                        visualData?.let { data ->
                            visualContent = VisualContentModel(
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                imageUrl = data["imageUrl"] as? String ?: "",
                                category = data["category"] as? String ?: "",
                                downloadUrl = data["downloadUrl"] as? String ?: ""
                            )

                            visualContent?.let { content ->
                                with(binding) {
                                    tvVisualTitle.text = content.title
                                    tvVisualDescription.text = content.description

                                    if (content.imageUrl.isNotEmpty()) {
                                        Picasso.get()
                                            .load(content.imageUrl)
                                            .placeholder(R.drawable.placeholder_infographic)
                                            .error(R.drawable.placeholder_infographic)
                                            .into(ivEducationalVisual)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        listeners.add(listener)
    }

    private fun loadExpertQuotes() {
        val listener = firestore.collection("educational_resources")
            .document("expert_quotes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading quotes", error)
                    showError("Failed to load quotes")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        val quotesData = document.data?.get("quotes") as? List<Map<String, Any>>
                        quotesData?.let { quotesList ->
                            allQuotes = quotesList.map { quoteMap ->
                                QuoteModel(
                                    id = quoteMap["id"] as? String ?: "",
                                    quote = quoteMap["quote"] as? String ?: "",
                                    authorName = quoteMap["authorName"] as? String ?: "",
                                    authorDesignation = quoteMap["authorDesignation"] as? String ?: "",
                                    authorImageUrl = quoteMap["authorImageUrl"] as? String ?: "",
                                    category = quoteMap["category"] as? String ?: ""
                                )
                            }

                            if (allQuotes.isNotEmpty()) {
                                expertQuote = allQuotes[Random.nextInt(allQuotes.size)]
                                expertQuote?.let { quote ->
                                    with(binding) {
                                        tvExpertQuote.text = quote.quote
                                        tvMentorName.text = quote.authorName
                                        tvMentorDesignation.text = quote.authorDesignation

                                        if (quote.authorImageUrl.isNotEmpty()) {
                                            Picasso.get()
                                                .load(quote.authorImageUrl)
                                                .placeholder(R.drawable.placeholder_avatar)
                                                .error(R.drawable.placeholder_avatar)
                                                .into(ivMentorAvatar)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        listeners.add(listener)
    }

    private fun getCurrentDateKey(): String {
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun openUrlInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showError("Unable to open link")
        }
    }

    private fun shareVisualContent() {
        visualContent?.let { content ->
            val shareText = "${content.title}\n${content.description}\n\nShared from UHA Educational App"
            shareContent(shareText)
        }
    }

    private fun shareQuote() {
        expertQuote?.let { quote ->
            val shareText = "\"${quote.quote}\"\n- ${quote.authorName}, ${quote.authorDesignation}\n\nShared from UHA Educational App"
            shareContent(shareText)
        }
    }

    private fun shareFact() {
        didYouKnowFact?.let { fact ->
            val shareText = "Did you know? ${fact.content}\n\nCategory: ${fact.category}\nShared from UHA Educational App"
            shareContent(shareText)
        }
    }

    private fun shareContent(content: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            showError("Unable to share content")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove all Firestore listeners
        listeners.forEach { it.remove() }
        listeners.clear()
        _binding = null
    }

    // Data Models for Firestore
    data class CareerGuidanceModel(
        val content: String = "",
        val readMoreUrl: String = "",
        val category: String = "",
        val lastUpdated: Any? = null
    )

    data class HistoryModel(
        val content: String = "",
        val featuredYear: String = "",
        val imageUrl: String = "",
        val category: String = ""
    )

    data class SpecialEventModel(
        val title: String = "",
        val description: String = "",
        val emoji: String = "",
        val category: String = ""
    )

    data class FactModel(
        val id: String = "",
        val content: String = "",
        val category: String = "",
        val difficulty: String = ""
    )

    data class VisualContentModel(
        val title: String = "",
        val description: String = "",
        val imageUrl: String = "",
        val category: String = "",
        val downloadUrl: String = ""
    )

    data class QuoteModel(
        val id: String = "",
        val quote: String = "",
        val authorName: String = "",
        val authorDesignation: String = "",
        val authorImageUrl: String = "",
        val category: String = ""
    )

    companion object {
        private const val TAG = "ResourcesFragment"

        fun newInstance() = ResourcesFragment()
    }
}