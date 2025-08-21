import unittest
import json
from unittest.mock import patch, MagicMock
from app import app

class AppSecurityTest(unittest.TestCase):
    def setUp(self):
        self.app = app.test_client()
        self.app.testing = True

    @patch('subprocess.run')
    def test_test_connection_command_injection_prevention(self, mock_run):
        # Setup mock response
        mock_process = MagicMock()
        mock_process.returncode = 0
        mock_process.stdout = "Mock ping output"
        mock_process.stderr = ""
        mock_run.return_value = mock_process

        # Test with a normal URL
        response = self.app.post('/testConnection', 
                               data=json.dumps({'url': 'example.com'}),
                               content_type='application/json')
        
        # Check that subprocess.run was called with the correct arguments
        mock_run.assert_called_with(
            ['ping', '-c', '1', 'example.com'], 
            shell=False,  # This is the key security fix - shell=False
            capture_output=True, 
            text=True, 
            timeout=30
        )
        
        # Test with a URL containing command injection attempt
        response = self.app.post('/testConnection', 
                               data=json.dumps({'url': 'example.com; cat /etc/passwd'}),
                               content_type='application/json')
        
        # The command should be passed as a single argument, not interpreted by shell
        mock_run.assert_called_with(
            ['ping', '-c', '1', 'example.com; cat /etc/passwd'], 
            shell=False,
            capture_output=True, 
            text=True, 
            timeout=30
        )
        
        # The ping command will fail with this input, but the important thing is that
        # the command injection is not executed because shell=False

if __name__ == '__main__':
    unittest.main()