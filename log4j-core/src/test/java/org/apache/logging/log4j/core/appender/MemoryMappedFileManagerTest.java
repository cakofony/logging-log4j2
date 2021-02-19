/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.appender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the MemoryMappedFileManager class.
 *
 * @since 2.1
 */
public class MemoryMappedFileManagerTest {

    @TempDir
    File tempDir;

    @Test
    public void testRemapAfterInitialMapSizeExceeded() throws IOException {
        final int mapSize = 64; // very small, on purpose
        final File file = new File(tempDir, "memory-mapped-file.bin");

        final boolean append = false;
        final boolean immediateFlush = false;
        try (final MemoryMappedFileManager manager = MemoryMappedFileManager.getFileManager(file.getAbsolutePath(),
                append, immediateFlush, mapSize, null, null)) {
            byte[] msg;
            for (int i = 0; i < 1000; i++) {
                msg = ("Message " + i + "\n").getBytes();
                manager.write(msg, 0, msg.length, false);
            }

        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            for (int i = 0; i < 1000; i++) {
                assertThat(line).describedAs("line").isNotNull();
                assertTrue(line.contains("Message " + i), "line incorrect");
                line = reader.readLine();
            }
        }
    }

    @Test
    public void testAppendDoesNotOverwriteExistingFile() throws IOException {
        final File file = new File(tempDir, "memory-mapped-file.bin");

        final int initialLength = 4 * 1024;

        // create existing file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[initialLength], 0, initialLength);
            fos.flush();
        }
        assertThat(file.length()).describedAs("all flushed to disk").isEqualTo(initialLength);

        final boolean isAppend = true;
        final boolean immediateFlush = false;
        try (final MemoryMappedFileManager manager = MemoryMappedFileManager.getFileManager(file.getAbsolutePath(),
                isAppend, immediateFlush, MemoryMappedFileManager.DEFAULT_REGION_LENGTH, null, null)) {
            manager.writeBytes(new byte[initialLength], 0, initialLength);
        }
        final int expected = initialLength * 2;
        assertThat(file.length()).describedAs("appended, not overwritten").isEqualTo(expected);
    }
}
