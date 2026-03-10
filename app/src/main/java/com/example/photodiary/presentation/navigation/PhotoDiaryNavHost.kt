package com.example.photodiary.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photodiary.presentation.calendar.CalendarScreen
import com.example.photodiary.presentation.calendar.CalendarViewModel
import com.example.photodiary.presentation.detail.DetailScreen
import com.example.photodiary.presentation.detail.DetailViewModel
import com.example.photodiary.presentation.main.MainScreen
import com.example.photodiary.presentation.main.MainViewModel
import com.example.photodiary.presentation.mypage.MyPageScreen
import com.example.photodiary.presentation.mypage.MyPageViewModel
import com.example.photodiary.presentation.write.WriteScreen
import com.example.photodiary.presentation.write.WriteViewModel

@Composable
fun PhotoDiaryNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PhotoDiaryDestinations.MAIN
    ) {
        composable(PhotoDiaryDestinations.MAIN) {
            val viewModel: MainViewModel = hiltViewModel()
            MainScreen(
                viewModel = viewModel,
                onNavigateCalendar = { navController.navigate(PhotoDiaryDestinations.CALENDAR) },
                onNavigateWrite = { navController.navigate(PhotoDiaryDestinations.WRITE) },
                onNavigateDetail = { navController.navigate(PhotoDiaryDestinations.DETAIL) },
                onNavigateMyPage = { navController.navigate(PhotoDiaryDestinations.MY_PAGE) }
            )
        }

        composable(PhotoDiaryDestinations.CALENDAR) {
            val viewModel: CalendarViewModel = hiltViewModel()
            CalendarScreen(viewModel = viewModel)
        }

        composable(PhotoDiaryDestinations.WRITE) {
            val viewModel: WriteViewModel = hiltViewModel()
            WriteScreen(viewModel = viewModel)
        }

        composable(PhotoDiaryDestinations.DETAIL) {
            val viewModel: DetailViewModel = hiltViewModel()
            DetailScreen(viewModel = viewModel)
        }

        composable(PhotoDiaryDestinations.MY_PAGE) {
            val viewModel: MyPageViewModel = hiltViewModel()
            MyPageScreen(viewModel = viewModel)
        }
    }
}
