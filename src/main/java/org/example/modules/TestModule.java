package org.example.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.example.inter.TestInter;
import org.example.inter.impl.TestInterImpl;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TestInter.class).annotatedWith(Names.named("testInter")).to(TestInterImpl.class);
    }
}
