package com.timelane.data.repository;

import com.timelane.data.local.TaskDao;
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
public final class TaskRepositoryImpl_Factory implements Factory<TaskRepositoryImpl> {
  private final Provider<TaskDao> daoProvider;

  public TaskRepositoryImpl_Factory(Provider<TaskDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public TaskRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static TaskRepositoryImpl_Factory create(Provider<TaskDao> daoProvider) {
    return new TaskRepositoryImpl_Factory(daoProvider);
  }

  public static TaskRepositoryImpl newInstance(TaskDao dao) {
    return new TaskRepositoryImpl(dao);
  }
}
