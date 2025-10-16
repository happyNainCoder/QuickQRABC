package com.example.quickqrabc.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickqrabc.R
import com.example.quickqrabc.adapter.HistoryAdapter
import com.example.quickqrabc.data.QRHistoryEntity
import com.example.quickqrabc.databinding.FragmentHistoryListBinding
import com.example.quickqrabc.viewmodel.QRViewModel

class HistoryListFragment : Fragment() {
    
    private var _binding: FragmentHistoryListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: QRViewModel
    private lateinit var adapter: HistoryAdapter
    private var historyType: String = "all"
    
    companion object {
        private const val ARG_TYPE = "type"
        
        fun newInstance(type: String): HistoryListFragment {
            val fragment = HistoryListFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            historyType = it.getString(ARG_TYPE) ?: "all"
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[QRViewModel::class.java]
        
        setupRecyclerView()
        observeHistory()
    }
    
    private fun setupRecyclerView() {
        adapter = HistoryAdapter { historyItem, view ->
            showPopupMenu(historyItem, view)
        }
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }
    
    private fun observeHistory() {
        val liveData = when (historyType) {
            "scan" -> viewModel.getScannedHistory()
            "generate" -> viewModel.getGeneratedHistory()
            else -> viewModel.getAllHistory()
        }
        
        liveData.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(historyList)
            }
        }
    }
    
    private fun showPopupMenu(historyItem: QRHistoryEntity, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.history_item_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_copy -> {
                    copyToClipboard(historyItem.content)
                    true
                }
                R.id.action_share -> {
                    shareText(historyItem.content)
                    true
                }
                R.id.action_delete -> {
                    viewModel.deleteHistory(historyItem)
                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    private fun shareText(text: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR Code Content"))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
