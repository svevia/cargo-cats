var builder = WebApplication.CreateBuilder(args);

var app = builder.Build();

// Ensure the uploads directory exists
var uploadsPath = Path.Combine(Directory.GetCurrentDirectory(), "uploads");
Directory.CreateDirectory(uploadsPath);
Console.WriteLine($"[DEBUG] Image service starting. Uploads directory: {uploadsPath}");

app.MapGet("/", () =>
{
    Console.WriteLine("[DEBUG] GET / - Retrieving photo list");
    try
    {
        // Get all image files from the uploads directory
        var imageExtensions = new[] { ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp" };
        var imageFiles = Directory.GetFiles(uploadsPath)
            .Where(file => imageExtensions.Contains(Path.GetExtension(file).ToLower()))
            .Select(file => new
            {
                filename = Path.GetFileName(file),
                path = Path.GetFileName(file),
                size = new FileInfo(file).Length,
                created = new FileInfo(file).CreationTime
            })
            .OrderByDescending(f => f.created)
            .ToList();

        Console.WriteLine($"[DEBUG] Found {imageFiles.Count} photos in uploads directory");
        return Results.Ok(new
        {
            message = "Image Service - Saved Photos",
            totalPhotos = imageFiles.Count,
            photos = imageFiles
        });
    }
    catch (Exception ex)
    {
        Console.WriteLine($"[ERROR] Failed to retrieve photos: {ex.Message}");
        return Results.Problem($"Error retrieving photos: {ex.Message}");
    }
});

app.MapPost("/savephoto", async (IFormFile file) =>
{
    Console.WriteLine($"[DEBUG] POST /savephoto - Received file upload request");
    if (file == null || file.Length == 0)
    {
        Console.WriteLine("[DEBUG] No file provided or file is empty");
        return Results.BadRequest("No file provided or file is empty");
    }

    var fileName = file.FileName;
    var filePath = Path.Combine(uploadsPath, fileName);
    Console.WriteLine($"[DEBUG] Saving file: {fileName} to {filePath} (size: {file.Length} bytes)");
    
    using (var stream = new FileStream(filePath, FileMode.Create))
    {
        await file.CopyToAsync(stream);
    }

    Console.WriteLine($"[DEBUG] Successfully saved file: {fileName}");
    // Return just the filename, not the full path
    return Results.Ok(new { path = fileName });
}).DisableAntiforgery();

app.MapGet("/getphoto", (string path) =>
{
    Console.WriteLine($"[DEBUG] GET /getphoto - Requested path: {path}");
    if (string.IsNullOrEmpty(path))
    {
        Console.WriteLine("[DEBUG] Path parameter is empty or null");
        return Results.BadRequest("Path parameter is required");
    }
    
    // Validate the path parameter to prevent path traversal
    if (IsPathTraversalAttempt(path))
    {
        Console.WriteLine($"[DEBUG] Path traversal attempt detected: {path}");
        return Results.BadRequest("Invalid file path");
    }
    
    var fullPath = Path.Combine(uploadsPath, path);
    Console.WriteLine($"[DEBUG] Full file path: {fullPath}");
    
    // Ensure the requested file is within the uploads directory
    if (!IsPathWithinDirectory(fullPath, uploadsPath))
    {
        Console.WriteLine($"[DEBUG] Path traversal attempt detected: {path}");
        return Results.BadRequest("Invalid file path");
    }

    if (!File.Exists(fullPath))
    {
        Console.WriteLine($"[DEBUG] File not found: {fullPath}");
        return Results.NotFound("File not found");
    }

    // Determine content type based on file extension
    var contentType = Path.GetExtension(fullPath).ToLower() switch
    {
        ".jpg" or ".jpeg" => "image/jpeg",
        ".png" => "image/png",
        ".gif" => "image/gif",
        ".bmp" => "image/bmp",
        ".webp" => "image/webp",
        ".txt" => "text/plain",
        ".json" => "application/json",
        ".xml" => "application/xml",
        ".config" => "text/plain",
        _ => "application/octet-stream"
    };
    Console.WriteLine($"[DEBUG] Serving file with content type: {contentType}");
    var fileBytes = File.ReadAllBytes(fullPath);
    Console.WriteLine($"[DEBUG] File size: {fileBytes.Length} bytes");
    return Results.File(fileBytes, contentType);
});

Console.WriteLine("[DEBUG] Starting image service application...");
app.Run();

// Helper method to check if a path contains traversal sequences
bool IsPathTraversalAttempt(string path)
{
    // Check for common path traversal patterns
    return path.Contains("..") || 
           path.Contains("/") || 
           path.Contains("\\") || 
           path.StartsWith("~") || 
           Path.IsPathRooted(path);
}

// Helper method to validate that a path is within a specified directory
bool IsPathWithinDirectory(string fullPath, string directory)
{
    try
    {
        // Normalize both paths to handle any directory traversal attempts
        var normalizedPath = Path.GetFullPath(fullPath);
        var normalizedDirectory = Path.GetFullPath(directory);
        
        // Check if the normalized path starts with the normalized directory
        return normalizedPath.StartsWith(normalizedDirectory, StringComparison.OrdinalIgnoreCase);
    }
    catch (Exception ex)
    {
        Console.WriteLine($"[ERROR] Path validation error: {ex.Message}");
        return false;
    }
}
