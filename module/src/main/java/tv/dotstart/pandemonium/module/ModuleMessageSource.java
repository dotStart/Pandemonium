/*
 * Copyright 2017 Johannes Donath <me@dotstart.tv>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.pandemonium.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;

/**
 * Provides a modular message source which resolves against multiple child sources.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class ModuleMessageSource extends AbstractMessageSource implements ConfigurationAwareMessageSource {
    private final LinkedList<MessageSource> childSources = new LinkedList<>();
    private final ReadWriteLock childSourceLock = new ReentrantReadWriteLock();

    private final Locale locale = Locale.ENGLISH; // TODO: Poll from configuration

    @Autowired
    public ModuleMessageSource(@Nonnull MessageSource uiMessageSource) {
        this.childSources.add(uiMessageSource);
    }

    /**
     * Adds a message source to the list of children.
     */
    public void addSource(@Nonnull MessageSource source) {
        this.childSourceLock.writeLock().lock();

        try {
            this.childSources.add(source);
        } finally {
            this.childSourceLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Locale getConfiguredLocale() {
        return this.locale;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getMessage(@Nonnull String code, @Nonnull Object... arguments) {
        return this.getMessage(code, arguments, this.locale);
    }

    /**
     * Removes a message source from the list of children.
     */
    public void removeSource(@Nonnull MessageSource source) {
        this.childSourceLock.writeLock().lock();

        try {
            this.childSources.remove(source);
        } finally {
            this.childSourceLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        this.childSourceLock.readLock().lock();

        try {
            return this.childSources.stream()
                    .flatMap((s) -> {
                        try {
                            return Stream.of(s.getMessage(code, new Object[0], locale));
                        } catch (NoSuchMessageException ex) {
                            return Stream.empty();
                        }
                    })
                    .map((m) -> this.createMessageFormat(m, locale))
                    .findAny()
                    .orElseGet(() -> this.createMessageFormat(code, locale));
        } finally {
            this.childSourceLock.readLock().unlock();
        }
    }
}
