/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.paging.mediator.list

import androidx.paging.ExperimentalPagingApi
import com.twidere.services.microblog.ListsService
import com.twidere.services.microblog.model.IUser
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.model.ui.UiUser
import com.twidere.twiderex.paging.crud.PagingMemoryCache

@ExperimentalPagingApi
class ListsMembersMediator(
    memoryCache: PagingMemoryCache<UiUser>,
    userKey: MicroBlogKey,
    private val service: ListsService,
    private val listId: String,
) : ListsUserPagingMediator(userKey, memoryCache) {
    override suspend fun loadUsers(key: String?, count: Int): List<IUser> {
        return service.listMembers(listId = listId, count = count, key)
    }
}
