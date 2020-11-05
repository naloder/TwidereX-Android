/*
 *  TwidereX
 *
 *  Copyright (C) 2020 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of TwidereX.
 * 
 *  TwidereX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  TwidereX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with TwidereX. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.component

import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import androidx.navigation.compose.navigate
import com.twidere.twiderex.annotations.IncomingComposeUpdate
import com.twidere.twiderex.component.lazy.itemsGridIndexed
import com.twidere.twiderex.extensions.navViewModel
import com.twidere.twiderex.extensions.withElevation
import com.twidere.twiderex.model.ui.UiMedia
import com.twidere.twiderex.model.ui.UiStatus
import com.twidere.twiderex.model.ui.UiUser
import com.twidere.twiderex.ui.AmbientNavController
import com.twidere.twiderex.ui.standardPadding
import com.twidere.twiderex.viewmodel.user.UserFavouriteTimelineViewModel
import com.twidere.twiderex.viewmodel.user.UserTimelineViewModel
import com.twidere.twiderex.viewmodel.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@IncomingComposeUpdate
@OptIn(ExperimentalLazyDsl::class)
@Composable
fun UserComponent(
    screenName: String,
    initialData: UiUser? = null,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val viewModel = navViewModel<UserViewModel>()
    val timelineViewModel = navViewModel<UserTimelineViewModel>()
    val favouriteViewModel = navViewModel<UserFavouriteTimelineViewModel>()

    val refreshing by viewModel.refreshing.observeAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val viewModelUser by viewModel.user.observeAsState(initial = initialData)
    val timeline by timelineViewModel.timeline.observeAsState(initial = emptyList())
    val timelineLoadingMore by timelineViewModel.loadingMore.observeAsState(initial = false)
    val mediaTimeline =
        timeline.filter { it.hasMedia }.flatMap { it.media.map { media -> media to it } }
    val favourite by favouriteViewModel.timeline.observeAsState(initial = emptyList())
    val favouriteLoadingMore by favouriteViewModel.loadingMore.observeAsState(initial = false)
    val tabs = listOf(
        Icons.Default.List,
        Icons.Default.Image,
        Icons.Default.Favorite,
    )
    val (selectedItem, setSelectedItem) = savedInstanceState { 0 }

    LaunchedTask(
        selectedItem,
        timeline,
        favourite,
    ) {
        viewModel.init(screenName, initialData)
        viewModelUser?.let { user ->
            async {
                when {
                    selectedItem == 0 && !timeline.any() -> timelineViewModel.refresh(user)
                    selectedItem == 1 && !mediaTimeline.any() -> timelineViewModel.loadMore(user)
                    selectedItem == 2 && !favourite.any() -> favouriteViewModel.refresh(user)
                }
            }
        }
    }
    viewModelUser?.let { user ->
        Box {
            val shouldStickyHeaderShown = lazyListState.firstVisibleItemIndex >= 1
            Surface(
                modifier = Modifier.zIndex(1f),
                elevation = if (shouldStickyHeaderShown) TopAppBarElevation else 0.dp
            ) {
                if (shouldStickyHeaderShown) {
                    TabsComponent(
                        items = tabs,
                        selectedItem = selectedItem,
                        onItemSelected = {
                            setSelectedItem(it)
                        },
                    )
                }
            }

            SwipeToRefreshLayout(
                refreshingState = refreshing,
                onRefresh = {
                    coroutineScope.launch {
                        viewModel.refresh(screenName)
                    }
                },
            ) {
                LazyColumn(
                    state = lazyListState,
                ) {
                    item {
                        UserInfo(user = user)
                    }

                    item {
                        TabsComponent(
                            items = tabs,
                            selectedItem = selectedItem,
                            onItemSelected = {
                                setSelectedItem(it)
                            },
                        )
                    }

                    when (selectedItem) {
                        0 -> {
                            if (timeline.any()) {
                                statusTimeline(
                                    timeline,
                                    timelineLoadingMore,
                                    coroutineScope,
                                    timelineViewModel,
                                    user
                                )
                            }
                        }
                        1 -> {
                            if (mediaTimeline.any()) {
                                mediaTimeline(
                                    mediaTimeline,
                                    timelineLoadingMore,
                                    coroutineScope,
                                    timelineViewModel,
                                    user,
                                )
                            }
                        }
                        2 -> {
                            if (favourite.any()) {
                                likeTimeline(
                                    favourite,
                                    timelineLoadingMore,
                                    coroutineScope,
                                    favouriteViewModel,
                                    user
                                )
                            }
                        }
                    }
                    if (timelineLoadingMore || favouriteLoadingMore) {
                        item {
                            val modifier = when (selectedItem) {
                                0, 1 -> if (timeline.any()) Modifier.fillParentMaxWidth() else Modifier.fillParentMaxSize()
                                2 -> if (favourite.any()) Modifier.fillParentMaxWidth() else Modifier.fillParentMaxSize()
                                else -> Modifier.fillParentMaxWidth()
                            }
                            StatusDivider()
                            Box(
                                modifier = modifier,
                                alignment = Alignment.TopCenter
                            ) {
                                LoadingProgress()
                            }
                        }
                    }


                }
            }
        }

    } ?: run {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingProgress()
        }
    }
}

private fun LazyListScope.likeTimeline(
    favourite: List<UiStatus>,
    timelineLoadingMore: Boolean,
    coroutineScope: CoroutineScope,
    favouriteViewModel: UserFavouriteTimelineViewModel,
    user: UiUser
) {
    itemsIndexed(favourite) { index, item ->
        Column {
            if (!timelineLoadingMore && index == favourite.lastIndex) {
                coroutineScope.launch {
                    favouriteViewModel.loadMore(user)
                }
            }
            TimelineStatusComponent(item)
            if (index != favourite.lastIndex) {
                StatusDivider()
            }
        }
    }
}

private fun LazyListScope.statusTimeline(
    timeline: List<UiStatus>,
    timelineLoadingMore: Boolean,
    coroutineScope: CoroutineScope,
    timelineViewModel: UserTimelineViewModel,
    user: UiUser
) {
    itemsIndexed(timeline) { index, item ->
        Column {
            if (!timelineLoadingMore && index == timeline.lastIndex) {
                coroutineScope.launch {
                    timelineViewModel.loadMore(user)
                }
            }
            TimelineStatusComponent(item)
            if (index != timeline.lastIndex) {
                StatusDivider()
            }
        }
    }
}

@IncomingComposeUpdate
private fun LazyListScope.mediaTimeline(
    items: List<Pair<UiMedia, UiStatus>>,
    timelineLoadingMore: Boolean,
    coroutineScope: CoroutineScope,
    timelineViewModel: UserTimelineViewModel,
    user: UiUser
) {
    item {
        Spacer(modifier = Modifier.height(standardPadding * 2))
    }
    itemsGridIndexed(
        items,
        rowSize = 2,
        spacing = standardPadding * 2,
        padding = standardPadding * 2,
    ) { index, item ->
        val navController = AmbientNavController.current
        if (!timelineLoadingMore && index == items.lastIndex) {
            coroutineScope.launch {
                timelineViewModel.loadMore(user)
            }
        }
        StatusMediaPreviewItem(
            item.first,
            modifier = Modifier
                .aspectRatio(1F)
                .clip(
                    MaterialTheme.shapes.medium
                ),
            onClick = {
                navController.navigate("media")
            }
        )
    }
    item {
        Spacer(modifier = Modifier.height(standardPadding * 2))
    }
}

@Composable
private fun UserInfo(user: UiUser) {
    val viewModel = navViewModel<UserViewModel>()
    val isMe by viewModel.isMe.observeAsState(initial = false)
    val relationship by viewModel.relationship.observeAsState(initial = null)
    val maxBannerSize = 200.dp
    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.surface.withElevation())
    ) {
        // TODO: parallax effect
        user.profileBackgroundImage?.let {
            Box(
                modifier = Modifier
                    .heightIn(max = maxBannerSize)
            ) {
                NetworkImage(
                    url = it,
                )
            }
        }
        Column {
            WithConstraints {
                Spacer(
                    modifier = Modifier.height(
                        min(
                            maxWidth * 160f / 320f - 72.dp / 2,
                            maxBannerSize - 72.dp / 2
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                alignment = Alignment.Center
            ) {
                Spacer(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clipToBounds()
                        .background(MaterialTheme.colors.surface.withElevation())
                )
                UserAvatar(
                    user = user,
                    size = 72.dp,
                )
            }
            Spacer(modifier = Modifier.height(standardPadding * 2))
            Row(
                modifier = Modifier
                    .padding(horizontal = standardPadding * 2)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "@${user.screenName}",
                    )
                }
                if (isMe) {
                    IconButton(onClick = {}) {
                        Icon(asset = Icons.Default.Edit)
                    }
                } else {
                    relationship?.let {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = if (it.followedBy) "Following" else "Follow",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.primary,
                            )
                            if (it.following) {
                                Text(
                                    text = "Follows you",
                                    style = MaterialTheme.typography.caption,
                                )
                            }
                        }
                    } ?: run {
                        CircularProgressIndicator()
                    }
                }
            }
            Spacer(modifier = Modifier.height(standardPadding))
            ListItem(
                text = {
                    Text(text = user.desc)
                }
            )
            user.website?.let {
                ListItem(
                    icon = {
                        Icon(asset = Icons.Default.Link)
                    },
                    text = {
                        Text(
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                )
            }
            user.location?.let {
                ListItem(
                    icon = {
                        Icon(asset = Icons.Default.MyLocation)
                    },
                    text = {
                        Text(
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(standardPadding * 2))
            Row {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = user.friendsCount.toString())
                    Text(text = "Following")
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = user.followersCount.toString())
                    Text(text = "Followers")
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = user.listedCount.toString())
                    Text(text = "Listed")
                }
            }
            Spacer(modifier = Modifier.height(standardPadding * 2))
        }
    }
}