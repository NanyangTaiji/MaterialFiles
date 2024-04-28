package me.zhanghai.android.files.storage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import me.zhanghai.android.files.util.Stateful;

public class AddLanSmbServerViewModel extends ViewModel {

    private final LanSmbServerListLiveData lanSmbServerListLiveData;

    public AddLanSmbServerViewModel() {
        lanSmbServerListLiveData = new LanSmbServerListLiveData();
    }

    public LiveData<Stateful<List<LanSmbServer>>> getLanSmbServerListLiveData() {
        return lanSmbServerListLiveData;
    }

    public void reload() {
        lanSmbServerListLiveData.loadValue();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        lanSmbServerListLiveData.close();
    }
}

