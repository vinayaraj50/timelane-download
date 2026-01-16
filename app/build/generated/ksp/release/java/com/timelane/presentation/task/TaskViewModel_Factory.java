package com.timelane.presentation.task;

import com.timelane.core.sound.SoundManager;
import com.timelane.core.sound.SoundPreferences;
import com.timelane.core.undo.UndoManager;
import com.timelane.domain.repository.TaskRepository;
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
public final class TaskViewModel_Factory implements Factory<TaskViewModel> {
  private final Provider<TaskRepository> repositoryProvider;

  private final Provider<UndoManager> undoManagerProvider;

  private final Provider<SoundManager> soundManagerProvider;

  private final Provider<SoundPreferences> soundPreferencesProvider;

  private final Provider<TaskPreferences> taskPreferencesProvider;

  public TaskViewModel_Factory(Provider<TaskRepository> repositoryProvider,
      Provider<UndoManager> undoManagerProvider, Provider<SoundManager> soundManagerProvider,
      Provider<SoundPreferences> soundPreferencesProvider,
      Provider<TaskPreferences> taskPreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.undoManagerProvider = undoManagerProvider;
    this.soundManagerProvider = soundManagerProvider;
    this.soundPreferencesProvider = soundPreferencesProvider;
    this.taskPreferencesProvider = taskPreferencesProvider;
  }

  @Override
  public TaskViewModel get() {
    return newInstance(repositoryProvider.get(), undoManagerProvider.get(), soundManagerProvider.get(), soundPreferencesProvider.get(), taskPreferencesProvider.get());
  }

  public static TaskViewModel_Factory create(Provider<TaskRepository> repositoryProvider,
      Provider<UndoManager> undoManagerProvider, Provider<SoundManager> soundManagerProvider,
      Provider<SoundPreferences> soundPreferencesProvider,
      Provider<TaskPreferences> taskPreferencesProvider) {
    return new TaskViewModel_Factory(repositoryProvider, undoManagerProvider, soundManagerProvider, soundPreferencesProvider, taskPreferencesProvider);
  }

  public static TaskViewModel newInstance(TaskRepository repository, UndoManager undoManager,
      SoundManager soundManager, SoundPreferences soundPreferences,
      TaskPreferences taskPreferences) {
    return new TaskViewModel(repository, undoManager, soundManager, soundPreferences, taskPreferences);
  }
}
