import sys
import os
import unittest
import json
from unittest.mock import patch, MagicMock
import pytest

# Add the parent directory to path so we can import the app
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app import app, is_valid_hostname

class TestAppSecurity(unittest.TestCase):
    
    def setUp(self):
        """Set up test client"""
        self.app = app.test_client()
        self.app.testing = True
        
    def test_hostname_validation_valid(self):
        """Test the hostname validation with valid hostnames"""
        valid_hostnames = [
            "example.com",
            "sub.domain.example.com",
            "192.168.1.1",
            "hostname",
            "host-name-with-dashes"
        ]
        
        for hostname in valid_hostnames:
            self.assertTrue(is_valid_hostname(hostname), f"Should validate {hostname} as valid")
            
    def test_hostname_validation_invalid(self):
        """Test the hostname validation with invalid hostnames"""
        invalid_hostnames = [
            "example.com; cat /etc/passwd",
            "example.com && cat /etc/passwd",
            "example.com | cat /etc/passwd",
            "example.com`cat /etc/passwd`",
            "example.com\ncat /etc/passwd",
            ";cat /etc/passwd",
            "$(cat /etc/passwd)"
        ]
        
        for hostname in invalid_hostnames:
            self.assertFalse(is_valid_hostname(hostname), f"Should validate {hostname} as invalid")
    
    def test_testConnection_endpoint_valid_input(self):
        """Test the /testConnection endpoint with valid input"""
        with patch('subprocess.run') as mock_run:
            # Configure the mock
            mock_process = MagicMock()
            mock_process.returncode = 0
            mock_process.stdout = "PING example.com (93.184.216.34): 56 data bytes\n"
            mock_process.stderr = ""
            mock_run.return_value = mock_process
            
            # Make the request
            response = self.app.post(
                '/testConnection',
                data=json.dumps({'url': 'example.com'}),
                content_type='application/json'
            )
            
            # Check response
            self.assertEqual(response.status_code, 200)
            data = json.loads(response.data)
            self.assertEqual(data['original_url'], 'example.com')
            self.assertTrue(data['success'])
            
            # Verify that subprocess.run was called with the correct arguments
            mock_run.assert_called_with(
                ['ping', '-c', '1', 'example.com'],
                shell=False,
                capture_output=True,
                text=True,
                timeout=30
            )
    
    def test_testConnection_endpoint_command_injection_attempt(self):
        """Test the /testConnection endpoint with command injection attempt"""
        # Make the request with a malicious URL
        response = self.app.post(
            '/testConnection',
            data=json.dumps({'url': 'example.com; cat /etc/passwd'}),
            content_type='application/json'
        )
        
        # Check that the request was rejected with 400 Bad Request
        self.assertEqual(response.status_code, 400)
        data = json.loads(response.data)
        self.assertEqual(data['error'], "Invalid hostname format")
    
    def test_testConnection_endpoint_missing_url(self):
        """Test the /testConnection endpoint with missing URL parameter"""
        # Make the request without a URL
        response = self.app.post(
            '/testConnection',
            data=json.dumps({}),
            content_type='application/json'
        )
        
        # Check that the request was rejected with 400 Bad Request
        self.assertEqual(response.status_code, 400)
        data = json.loads(response.data)
        self.assertEqual(data['error'], "URL parameter is required")

if __name__ == '__main__':
    unittest.main()