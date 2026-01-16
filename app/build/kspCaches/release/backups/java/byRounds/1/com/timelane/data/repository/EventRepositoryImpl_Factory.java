package com.timelane.data.repository;

import com.timelane.data.local.EventDao;
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
public final class EventRepositoryImpl_Factory implements Factory<EventRepositoryImpl> {
  private final Provider<EventDao> daoProvider;

  public EventRepositoryImpl_Factory(Provider<EventDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public EventRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static EventRepositoryImpl_Factory create(Provider<EventDao> daoProvider) {
    return new EventRepositoryImpl_Factory(daoProvider);
  }

  public static EventRepositoryImpl newInstance(EventDao dao) {
    return new EventRepositoryImpl(dao);
  }
}
