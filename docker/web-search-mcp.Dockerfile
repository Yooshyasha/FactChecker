FROM python:3.11-slim-bookworm

WORKDIR /app

# Install system dependencies required for building and playwright
RUN apt-get update && apt-get install -y \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
# Overrides the submodule's `mcp>=1.0.0`, which is pinned here rather than
# upstream because we have no write access to zydins/web-search-mcp-server.
# Both constraints go into one pip invocation so the resolver sees them together.
RUN pip install --no-cache-dir -r requirements.txt "mcp==1.29.0"

# Install Playwright browsers (Chromium only to save space)
RUN playwright install --with-deps chromium

COPY . .

# Default port
ENV PORT=8000

# Expose the port defined by the environment variable
EXPOSE $PORT

# Run the server using python directly so it picks up the PORT env var logic
CMD ["python", "web_search_mcp_server/server.py"]
