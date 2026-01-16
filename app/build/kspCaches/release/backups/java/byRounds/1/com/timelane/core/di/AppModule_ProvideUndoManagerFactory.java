package com.timelane.core.di;

import com.timelane.core.undo.UndoManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideUndoManagerFactory implements Factory<UndoManager> {
  @Override
  public UndoManager get() {
    return provideUndoManager();
  }

  public static AppModule_ProvideUndoManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static UndoManager provideUndoManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideUndoManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideUndoManagerFactory INSTANCE = new AppModule_ProvideUndoManagerFactory();
  }
}
