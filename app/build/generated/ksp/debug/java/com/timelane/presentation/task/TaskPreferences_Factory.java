package com.timelane.presentation.task;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class TaskPreferences_Factory implements Factory<TaskPreferences> {
  private final Provider<Context> contextProvider;

  public TaskPreferences_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TaskPreferences get() {
    return newInstance(contextProvider.get());
  }

  public static TaskPreferences_Factory create(Provider<Context> contextProvider) {
    return new TaskPreferences_Factory(contextProvider);
  }

  public static TaskPreferences newInstance(Context context) {
    return new TaskPreferences(context);
  }
}
