# Security Tests for Webhook Service

This directory contains security tests for the Webhook Service API.

## Running the Tests

To run the security tests:

```bash
cd /path/to/cargo-cats/services/webhookservice
python -m pytest tests/test_app_security.py -v
```

Or using unittest directly:

```bash
cd /path/to/cargo-cats/services/webhookservice
python -m unittest tests/test_app_security.py
```

## Test Coverage

The tests cover:

1. Hostname validation function to prevent command injection
2. Secure execution of subprocess without shell=True
3. Proper input validation for the `/testConnection` endpoint
4. Protection against command injection attack vectors