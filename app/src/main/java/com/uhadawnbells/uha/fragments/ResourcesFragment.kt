package com.uhadawnbells.uha.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import com.uhadawnbells.uha.R
class ResourcesFragment : Fragment() {

    // UI Components
    private lateinit var tvCareerGuidance: TextView
    private lateinit var tvHistoryContent: TextView
    private lateinit var tvHistoryDate: TextView
    private lateinit var tvHistoryYear: TextView
    private lateinit var tvSpecialEventTitle: TextView
    private lateinit var tvSpecialEventDescription: TextView
    private lateinit var tvDidYouKnow: TextView
    private lateinit var tvVisualTitle: TextView
    private lateinit var tvVisualDescription: TextView
    private lateinit var tvExpertQuote: TextView
    private lateinit var tvMentorName: TextView
    private lateinit var tvMentorDesignation: TextView
    private lateinit var ivEducationalVisual: ImageView
    private lateinit var ivMentorAvatar: ImageView
    private lateinit var btnReadMoreCareer: MaterialButton
    private lateinit var btnRefreshFact: MaterialButton

    // Firebase Firestore Reference
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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resources, container, false)

        initializeViews(view)
        initializeFirestore()
        setupClickListeners()
        loadDataFromFirestore()

        return view
    }

    private fun initializeViews(view: View) {
        // Career & Exam Guidance
        tvCareerGuidance = view.findViewById(R.id.tv_career_guidance)
        btnReadMoreCareer = view.findViewById(R.id.btn_read_more_career)

        // Today in History
        tvHistoryContent = view.findViewById(R.id.tv_history_content)
        tvHistoryDate = view.findViewById(R.id.tv_history_date)
        tvHistoryYear = view.findViewById(R.id.tv_history_year)

        // Festival/Special Events
        tvSpecialEventTitle = view.findViewById(R.id.tv_special_event_title)
        tvSpecialEventDescription = view.findViewById(R.id.tv_special_event_description)

        // Did You Know
        tvDidYouKnow = view.findViewById(R.id.tv_did_you_know)
        btnRefreshFact = view.findViewById(R.id.btn_refresh_fact)

        // Educational Visual
        tvVisualTitle = view.findViewById(R.id.tv_visual_title)
        tvVisualDescription = view.findViewById(R.id.tv_visual_description)
        ivEducationalVisual = view.findViewById(R.id.iv_educational_visual)

        // Expert Quotes
        tvExpertQuote = view.findViewById(R.id.tv_expert_quote)
        tvMentorName = view.findViewById(R.id.tv_mentor_name)
        tvMentorDesignation = view.findViewById(R.id.tv_mentor_designation)
        ivMentorAvatar = view.findViewById(R.id.iv_mentor_avatar)

        // Set current date in Indian format
        val dateFormat = SimpleDateFormat("dd MMMM", Locale("hi", "IN"))
        tvHistoryDate.text = dateFormat.format(Date())
    }

    private fun initializeFirestore() {
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setupClickListeners() {
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

        // Long click listeners for sharing Indian content
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

        // Click listener for history content sharing
        tvHistoryContent.setOnLongClickListener {
            shareHistoryContent()
            true
        }

        // Click listener for special event sharing
        tvSpecialEventTitle.setOnLongClickListener {
            shareSpecialEvent()
            true
        }
    }

    private fun loadDataFromFirestore() {
        loadCareerGuidance()
        loadTodayInHistory()
        loadSpecialEvents()
        loadDidYouKnowFacts()
        loadVisualContent()
        loadExpertQuotes()
        checkLastContentUpdate()
    }

    private fun loadCareerGuidance() {
        val listener = firestore.collection("educational_resources")
            .document("career_guidance")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading career guidance", error)
                    showError("भारतीय करियर गाइडेंस लोड नहीं हो सका")
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        careerGuidance = document.toObject(CareerGuidanceModel::class.java)
                        careerGuidance?.let {
                            tvCareerGuidance.text = it.content
                        }
                    } else {
                        // Fallback content for Indian students
                        tvCareerGuidance.text = "भारत में करियर के अनंत अवसर हैं! IIT-JEE, NEET, UPSC से लेकर डिजिटल इंडिया तक - हर क्षेत्र में सफलता के मार्ग खुले हैं।"
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
                    showError("आज का इतिहास लोड नहीं हो सका")
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
                                tvHistoryContent.text = history.content
                                tvHistoryYear.text = "मुख्य वर्ष: ${history.featuredYear}"
                            }
                        } ?: run {
                            // Fallback Indian history content
                            showFallbackIndianHistory()
                        }
                    } else {
                        showFallbackIndianHistory()
                    }
                }
            }
        listeners.add(listener)
    }

    private fun showFallbackIndianHistory() {
        tvHistoryContent.text = "भारत का गौरवशाली इतिहास हजारों वर्षों का है। सिंधु घाटी सभ्यता से लेकर आधुनिक भारत तक, हमारे पूर्वजों ने विज्ञान, कला, दर्शन और संस्कृति में अमूल्य योगदान दिया है।"
        tvHistoryYear.text = "मुख्य काल: प्राचीन से आधुनिक"
    }

    private fun loadSpecialEvents() {
        val todayKey = getCurrentDateKey()
        val listener = firestore.collection("educational_resources")
            .document("special_events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading special events", error)
                    showError("विशेष कार्यक्रम लोड नहीं हो सके")
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
                                tvSpecialEventTitle.text = event.title
                                tvSpecialEventDescription.text = event.description
                            }
                        } ?: run {
                            showFallbackIndianEvent()
                        }
                    } else {
                        showFallbackIndianEvent()
                    }
                }
            }
        listeners.add(listener)
    }

    private fun showFallbackIndianEvent() {
        tvSpecialEventTitle.text = "भारतीय शिक्षा दिवस"
        tvSpecialEventDescription.text = "भारत की समृद्ध शिक्षा परंपरा का सम्मान करें। गुरुकुल से आधुनिक विश्वविद्यालय तक, भारत में शिक्षा हमेशा सर्वोपरि रही है।"
    }

    private fun loadDidYouKnowFacts() {
        val listener = firestore.collection("educational_resources")
            .document("did_you_know_facts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading facts", error)
                    showError("रोचक तथ्य लोड नहीं हो सके")
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
                                    difficulty = factMap["difficulty"] as? String ?: "",
                                    tags = (factMap["tags"] as? List<String>) ?: emptyList()
                                )
                            }

                            if (allFacts.isNotEmpty()) {
                                didYouKnowFact = allFacts[Random.nextInt(allFacts.size)]
                                didYouKnowFact?.let {
                                    tvDidYouKnow.text = it.content
                                }
                            }
                        }
                    } else {
                        // Fallback Indian fact
                        tvDidYouKnow.text = "क्या आप जानते हैं? भारत में 0 (शून्य) की खोज हुई थी! आर्यभट्ट ने 5वीं शताब्दी में इस क्रांतिकारी अवधारणा की खोज की जिसने पूरी दुनिया के गणित को बदल दिया।"
                    }
                }
            }
        listeners.add(listener)
    }

    private fun loadRandomFact() {
        if (allFacts.isNotEmpty()) {
            didYouKnowFact = allFacts[Random.nextInt(allFacts.size)]
            didYouKnowFact?.let {
                tvDidYouKnow.text = it.content
            }
        } else {
            // Show random Indian fact
            val indianFacts = listOf(
                "भारत में 19,500+ भाषाएं बोली जाती हैं - यह दुनिया का सबसे भाषाई विविधता वाला देश है!",
                "शतरंज की खोज भारत में हुई थी, जिसे पहले 'चतुरंग' कहा जाता था।",
                "योग का जन्म भारत में 5000 साल पहले हुआ था और आज दुनियाभर में प्रसिद्ध है।",
                "भारत दुनिया का सबसे बड़ा डाकघर नेटवर्क रखता है - 1,55,000+ डाकघर!"
            )
            tvDidYouKnow.text = indianFacts[Random.nextInt(indianFacts.size)]
        }
    }

    private fun loadVisualContent() {
        val todayKey = getCurrentDateKey()
        val listener = firestore.collection("educational_resources")
            .document("visual_content")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading visual content", error)
                    showError("शैक्षिक दृश्य सामग्री लोड नहीं हो सकी")
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
                        } ?: run {
                            showFallbackIndianVisual()
                        }
                    } else {
                        showFallbackIndianVisual()
                    }
                }
            }
        listeners.add(listener)
    }

    private fun showFallbackIndianVisual() {
        tvVisualTitle.text = "भारतीय शिक्षा की परंपरा"
        tvVisualDescription.text = "प्राचीन गुरुकुल से आधुनिक विश्वविद्यालय तक भारतीय शिक्षा का सफर"
    }

    private fun loadExpertQuotes() {
        val listener = firestore.collection("educational_resources")
            .document("expert_quotes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error loading quotes", error)
                    showError("विशेषज्ञ उद्धरण लोड नहीं हो सके")
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
                                    category = quoteMap["category"] as? String ?: "",
                                    tags = (quoteMap["tags"] as? List<String>) ?: emptyList()
                                )
                            }

                            if (allQuotes.isNotEmpty()) {
                                expertQuote = allQuotes[Random.nextInt(allQuotes.size)]
                                expertQuote?.let { quote ->
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
                    } else {
                        // Fallback Indian quote
                        showFallbackIndianQuote()
                    }
                }
            }
        listeners.add(listener)
    }

    private fun showFallbackIndianQuote() {
        tvExpertQuote.text = "सर्वे भवन्तु सुखिनः सर्वे सन्तु निरामयाः। सबका कल्याण हो, सभी स्वस्थ रहें।"
        tvMentorName.text = "प्राचीन भारतीय मंत्र"
        tvMentorDesignation.text = "संस्कृत श्लोक"
    }

    private fun checkLastContentUpdate() {
        firestore.collection("educational_resources")
            .document("career_guidance")
            .get()
            .addOnSuccessListener { document ->
                val lastUpdated = document.getTimestamp("lastUpdated")
                Log.d(TAG, "Content last updated: $lastUpdated")

                // Check if content is older than 25 hours
                lastUpdated?.let { timestamp ->
                    val now = System.currentTimeMillis()
                    val hoursSinceUpdate = (now - timestamp.toDate().time) / (1000 * 60 * 60)

                    if (hoursSinceUpdate > 25) {
                        Log.w(TAG, "Content might be outdated. Hours since last update: $hoursSinceUpdate")
                        // Show message about content refresh
                        Toast.makeText(context, "सामग्री अपडेट हो रही है...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
            showError("लिंक खुल नहीं सका")
        }
    }

    private fun shareVisualContent() {
        visualContent?.let { content ->
            val shareText = "${content.title}\n${content.description}\n\nभारतीय शिक्षा ऐप UHA से साझा किया गया"
            shareContent(shareText)
        }
    }

    private fun shareQuote() {
        expertQuote?.let { quote ->
            val shareText = "\"${quote.quote}\"\n- ${quote.authorName}\n${quote.authorDesignation}\n\nभारतीय शिक्षा ऐप UHA से साझा किया गया"
            shareContent(shareText)
        }
    }

    private fun shareFact() {
        didYouKnowFact?.let { fact ->
            val shareText = "क्या आप जानते हैं? ${fact.content}\n\nविषय: ${fact.category}\nभारतीय शिक्षा ऐप UHA से साझा किया गया"
            shareContent(shareText)
        }
    }

    private fun shareHistoryContent() {
        todayInHistory?.let { history ->
            val shareText = "आज के दिन इतिहास में: ${history.content}\n\nमुख्य वर्ष: ${history.featuredYear}\nभारतीय शिक्षा ऐप UHA से साझा किया गया"
            shareContent(shareText)
        }
    }

    private fun shareSpecialEvent() {
        specialEvent?.let { event ->
            val shareText = "${event.title}\n${event.description}\n\nभारतीय शिक्षा ऐप UHA से साझा किया गया"
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
            startActivity(Intent.createChooser(shareIntent, "इसे साझा करें"))
        } catch (e: Exception) {
            showError("साझा नहीं कर सके")
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
    }

    // Data Models for Indian Educational Content
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
        val difficulty: String = "",
        val tags: List<String> = emptyList()
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
        val category: String = "",
        val tags: List<String> = emptyList()
    )

    companion object {
        private const val TAG = "ResourcesFragment"

        fun newInstance() = ResourcesFragment()
    }
}