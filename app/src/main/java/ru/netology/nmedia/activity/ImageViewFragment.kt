package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ImageViewBinding

class ImageViewFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ImageViewBinding.inflate(
            inflater,
            container,
            false
        )

        val imageUrl = arguments?.getString("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(binding.bigImage)

        requireActivity().addMenuProvider(
            object: MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.photo_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when(menuItem.itemId) {
                        R.id.return_back -> {
                            findNavController().navigateUp()
                            true
                        }
                        else -> false
                    }
            },
            viewLifecycleOwner
        )
        return binding.root
    }
}