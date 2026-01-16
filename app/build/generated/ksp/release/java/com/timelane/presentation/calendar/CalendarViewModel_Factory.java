package com.timelane.presentation.calendar;

import com.timelane.core.sound.SoundManager;
import com.timelane.core.sound.SoundPreferences;
import com.timelane.core.undo.UndoManager;
import com.timelane.domain.repository.EventRepository;
import com.timelane.presentation.task.TaskPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CalendarViewModel_Factory implements Factory<CalendarViewModel> {
  private final Provider<EventRepository> repositoryProvider;

  private final Provider<UndoManager> undoManagerProvider;

  private final Provider<TaskPreferences> taskPreferencesProvider;

  private final Provider<SoundManager> soundManagerProvider;

  private final Provider<SoundPreferences> soundPreferencesProvider;

  public CalendarViewModel_Factory(Provider<EventRepository> repositoryProvider,
      Provider<UndoManager> undoManagerProvider, Provider<TaskPreferences> taskPreferencesProvider,
      Provider<SoundManager> soundManagerProvider,
      Provider<SoundPreferences> soundPreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.undoManagerProvider = undoManagerProvider;
    this.taskPreferencesProvider = taskPreferencesProvider;
    this.soundManagerProvider = soundManagerProvider;
    this.soundPreferencesProvider = soundPreferencesProvider;
  }

  @Override
  public CalendarViewModel get() {
    return newInstance(repositoryProvider.get(), undoManagerProvider.get(), taskPreferencesProvider.get(), soundManagerProvider.get(), soundPreferencesProvider.get());
  }

  public static CalendarViewModel_Factory create(Provider<EventRepository> repositoryProvider,
      Provider<UndoManager> undoManagerProvider, Provider<TaskPreferences> taskPreferencesProvider,
      Provider<SoundManager> soundManagerProvider,
      Provider<SoundPreferences> soundPreferencesProvider) {
    return new CalendarViewModel_Factory(repositoryProvider, undoManagerProvider, taskPreferencesProvider, soundManagerProvider, soundPreferencesProvider);
  }

  public static CalendarViewModel newInstance(EventRepository repository, UndoManager undoManager,
      TaskPreferences taskPreferences, SoundManager soundManager,
      SoundPreferences soundPreferences) {
    return new CalendarViewModel(repository, undoManager, taskPreferences, soundManager, soundPreferences);
  }
}
