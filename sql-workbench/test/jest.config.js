/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

module.exports = {
  rootDir: "../",
  setupFiles: ["<rootDir>/test/polyfills.ts", "<rootDir>/test/setupTests.ts"],
  setupFilesAfterEnv: ["<rootDir>/test/setup.jest.ts"],
  roots: ["<rootDir>"],
  coverageDirectory: "./coverage",
  moduleNameMapper: {
    "\\.(css|less|scss)$": "<rootDir>/test/mocks/styleMock.ts",
    "^ui/(.*)": "<rootDir>/../../src/ui/public/$1/",
  },
  coverageReporters: ["lcov", "text", "cobertura"],
  testMatch: ["**/*.test.ts", "**/*.test.tsx"],
  collectCoverageFrom: [
    "!**/*.ts",
    "**/*.tsx",
    "!**/*.js",
    "!**/*.jsx",
    "!**/models/**",
    "!**/node_modules/**",
    "!**/index.ts",
    "!<rootDir>/index.js",
    "!<rootDir>/public/app.js",
    "!<rootDir>/public/temporary/**",
    "!<rootDir>/babel.config.js",
    "!<rootDir>/test/**",
    "!<rootDir>/server/**",
    "!<rootDir>/coverage/**",
    "!<rootDir>/scripts/**",
    "!<rootDir>/build/**",
    "!**/vendor/**",
  ],
  clearMocks: true,
  testPathIgnorePatterns: ["<rootDir>/build/", "<rootDir>/node_modules/"],
};
