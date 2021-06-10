package com.joshsoftware.core.repository

import com.joshsoftware.core.model.Address
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class AddressRepository @Inject constructor(val dbManager: FirebaseRealtimeDbManager) {

    fun saveAddress(memberId: String, groupId: String, address: Address): Deferred<Address> {
        return dbManager.saveAddress(memberId, groupId, address)
    }
}