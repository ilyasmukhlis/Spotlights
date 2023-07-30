package kz.ilyasmukhlis.spotlights

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kz.ilyasmukhlis.spotlights.databinding.FragmentSecondBinding
import kz.ilyasmukhlis.spotlights.spotlights.ui.SpotlightsDialog

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.showSpotlights(binding.root, 2)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    private fun observeViewModel() = with(viewModel) {
        spotlightItem.observe(viewLifecycleOwner) {
            if (it != null) {
                val (items, code) = it
                val dialog = SpotlightsDialog()
                dialog.onCompletion = {
                    Toast.makeText(context, "Test finished with code = $code", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.onCompletion()
                }
                dialog.show(
                    requireActivity(),
                    childFragmentManager,
                    items
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}