package com.timelane.core.sound;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SoundPreferences_Factory implements Factory<SoundPreferences> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public SoundPreferences_Factory(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public SoundPreferences get() {
    return newInstance(dataStoreProvider.get());
  }

  public static SoundPreferences_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new SoundPreferences_Factory(dataStoreProvider);
  }

  public static SoundPreferences newInstance(DataStore<Preferences> dataStore) {
    return new SoundPreferences(dataStore);
  }
}
