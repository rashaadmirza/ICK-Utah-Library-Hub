package edu.northeastern.ickutah;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import edu.northeastern.ickutah.ui.AboutFragment;
import edu.northeastern.ickutah.ui.BookIssuesFragment;
import edu.northeastern.ickutah.ui.HomeFragment;
import edu.northeastern.ickutah.ui.LibraryFragment;
import edu.northeastern.ickutah.ui.ReadersFragment;
import edu.northeastern.ickutah.ui.SearchFragment;
import edu.northeastern.ickutah.utils.UiUtils;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); // Hide the toolbar title

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageView menuIcon = findViewById(R.id.menu_icon);

        // Open Drawer when clicking Menu Icon
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Handle Navigation Drawer Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            handleDrawerClick(item);
            return true;
        });

        // Handle Bottom Navigation Clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleBottomNavClick(item);
            return true;
        });

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    /** @noinspection DataFlowIssue*/
    // Drawer Handling
    private void handleDrawerClick(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        Integer selectedItemId = null; // Track Bottom Nav item

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            selectedItemId = R.id.nav_home;
        } else if (itemId == R.id.nav_readers) {
            selectedFragment = new ReadersFragment();
            selectedItemId = R.id.nav_readers;
        } else if (itemId == R.id.nav_library) {
            selectedFragment = new LibraryFragment();
            selectedItemId = R.id.nav_library;
        } else if (itemId == R.id.nav_book_issues) {
            selectedFragment = new BookIssuesFragment();
            selectedItemId = R.id.nav_book_issues;
        } else if (itemId == R.id.nav_search) {
            selectedFragment = new SearchFragment();
            selectedItemId = R.id.nav_search;
        } else if (itemId == R.id.nav_about) {
            selectedFragment = new AboutFragment();
            selectedItemId = null; // No selection in Bottom Nav
        } else if (itemId == R.id.nav_add_book) {
            selectedFragment = new LibraryFragment();
            selectedItemId = R.id.nav_library;

            // Load fragment first, then trigger AddBookDialog
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            // Delay the dialog trigger slightly to ensure the fragment is loaded
            new android.os.Handler().postDelayed(() -> {
                LibraryFragment libraryFragment = (LibraryFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (libraryFragment != null) {
                    libraryFragment.showAddBookDialog();
                }
            }, 300);
        } else if (itemId == R.id.nav_add_reader) {
            selectedFragment = new ReadersFragment();
            selectedItemId = R.id.nav_readers;

            // Load fragment first, then trigger AddReaderDialog
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            // Delay the dialog trigger slightly to ensure the fragment is loaded
            new android.os.Handler().postDelayed(() -> {
                ReadersFragment readersFragment = (ReadersFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (readersFragment != null) {
                    readersFragment.showAddReaderDialog();
                }
            }, 300);
        } else if (itemId == R.id.nav_issue_book) {
            selectedFragment = new LibraryFragment();
            selectedItemId = R.id.nav_library;

            // Load fragment first, then trigger AddBookDialog
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            // Delay the toast trigger slightly to ensure the fragment is loaded
            new android.os.Handler().postDelayed(() -> {
                LibraryFragment libraryFragment = (LibraryFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (libraryFragment != null) {
                    UiUtils.showToast(this, "Select a book");
                }
            }, 300);
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        // Update Bottom Navigation selection
        if (selectedItemId != null) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        } else {
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {  // Uncheck all items manually if the fragment is not in Bottom Nav
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    // Bottom Navigation Handling
    private void handleBottomNavClick(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String title = "";
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            title = "Home";
        } else if (itemId == R.id.nav_readers) {
            selectedFragment = new ReadersFragment();
            title = "Users";
        } else if (itemId == R.id.nav_library) {
            selectedFragment = new LibraryFragment();
            title = "Library";
        } else if (itemId == R.id.nav_book_issues) {
            selectedFragment = new BookIssuesFragment();
            title = "Book Issues";
        } else if (itemId == R.id.nav_search) {
            selectedFragment = new SearchFragment();
            title = "Search";
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        // Set Toolbar Title
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }
}