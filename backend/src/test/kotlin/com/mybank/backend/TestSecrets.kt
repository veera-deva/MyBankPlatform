package com.mybank.backend

// Test-only JWT signing secret. Deliberately hardcoded and unrelated to any real
// dev/staging/prod secret (which comes from the JWT_SECRET env var, see application.yaml).
// Randomly generated once via `openssl rand -hex 32` (64 hex chars -> 64 raw UTF-8 bytes
// -> 512 bits), comfortably above jjwt's 256-bit HMAC minimum.
// Shared across test classes via @TestPropertySource so every test's JwtService bean gets
// a real, sufficiently strong key with no placeholder/env-var resolution involved at all.
const val TEST_JWT_SECRET = "6635cf09b6789a4a2b7d5efdaad123ca8ce24d356ddbbf0518e60be08304e894"
