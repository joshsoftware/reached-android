package com.joshsoftware.reached.viewmodel

import com.joshsoftware.core.model.Address
import com.joshsoftware.core.repository.AddressRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class SaveLocationViewModel @Inject constructor(val addressRepository: AddressRepository): BaseViewModel<Address>() {


    fun saveAddress(memberId: String, groupId: String, address: Address) {
        executeRoutine {
            _result.value = addressRepository.saveAddress(memberId, groupId, address).await()
        }
    }

}