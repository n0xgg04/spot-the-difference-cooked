.PHONY: help build clean db server client admin all stop logs

MAVEN := mvn
DOCKER_COMPOSE := docker-compose

help:
	@echo "╔════════════════════════════════════════════════════════════════╗"
	@echo "║           Spot The Difference - Makefile Commands            ║"
	@echo "╠════════════════════════════════════════════════════════════════╣"
	@echo "║  make build         - Build tất cả modules                    ║"
	@echo "║  make clean         - Clean build artifacts                   ║"
	@echo "║  make db            - Start MySQL database                    ║"
	@echo "║  make db-stop       - Stop MySQL database                     ║"
	@echo "║  make server        - Run game server                         ║"
	@echo "║  make client        - Run game client                         ║"
	@echo "║  make admin         - Run admin uploader                      ║"
	@echo "║  make all           - Start DB + Server + Client              ║"
	@echo "║  make run           - Same as 'make all'                      ║"
	@echo "║  make stop          - Stop all running services               ║"
	@echo "║  make logs          - Show docker-compose logs                ║"
	@echo "║  make dev           - Development mode (DB + Server)          ║"
	@echo "╚════════════════════════════════════════════════════════════════╝"

build:
	@echo "🔨 Building all modules..."
	$(MAVEN) clean install -DskipTests
	@echo "✅ Build completed!"

clean:
	@echo "🧹 Cleaning build artifacts..."
	$(MAVEN) clean
	@echo "✅ Clean completed!"

db:
	@echo "🗄️  Starting MySQL database..."
	$(DOCKER_COMPOSE) up -d db
	@echo "⏳ Waiting for database to be ready..."
	@sleep 5
	$(DOCKER_COMPOSE) exec db mysqladmin ping -h 127.0.0.1 -proot || true
	@echo "✅ Database is running on port 3306"

db-stop:
	@echo "🛑 Stopping MySQL database..."
	$(DOCKER_COMPOSE) down
	@echo "✅ Database stopped!"

server: build
	@echo "🚀 Starting game server..."
	@cd server && java -jar target/server-0.1.0-SNAPSHOT.jar

client: build
	@echo "🎮 Starting game client..."
	@cd client && $(MAVEN) javafx:run

admin: build
	@echo "⚙️  Starting admin uploader..."
	@cd admin && java -jar target/admin-0.1.0-SNAPSHOT.jar

dev: db
	@echo "💻 Development mode: Starting server..."
	@sleep 2
	@cd server && $(MAVEN) exec:java

all: build db
	@echo "🌟 Starting all services..."
	@echo "📊 Database is ready"
	@echo "🚀 Starting server in background..."
	@cd server && java -jar target/server-0.1.0-SNAPSHOT.jar & echo $$! > /tmp/spotgame-server.pid
	@sleep 3
	@echo "🎮 Starting client..."
	@cd client && $(MAVEN) javafx:run

run: all

stop:
	@echo "🛑 Stopping all services..."
	@if [ -f /tmp/spotgame-server.pid ]; then \
		kill `cat /tmp/spotgame-server.pid` 2>/dev/null || true; \
		rm /tmp/spotgame-server.pid; \
		echo "✅ Server stopped"; \
	fi
	@pkill -f "com.ltm.game.client.ClientApp" 2>/dev/null || true
	@echo "✅ Client stopped"
	@$(DOCKER_COMPOSE) down
	@echo "✅ Database stopped"
	@echo "✅ All services stopped!"

logs:
	@echo "📋 Showing database logs..."
	$(DOCKER_COMPOSE) logs -f db

db-reset: db-stop
	@echo "⚠️  Removing database volumes..."
	$(DOCKER_COMPOSE) down -v
	@echo "🔄 Recreating database..."
	$(DOCKER_COMPOSE) up -d db
	@echo "✅ Database reset completed!"

test:
	@echo "🧪 Running tests..."
	$(MAVEN) test

install: build
	@echo "📦 Installing to local maven repository..."
	$(MAVEN) install

package:
	@echo "📦 Packaging applications..."
	$(MAVEN) package -DskipTests
	@echo "✅ Package completed!"
	@echo "📍 Server JAR: server/target/server-0.1.0-SNAPSHOT.jar"
	@echo "📍 Client JAR: client/target/client-0.1.0-SNAPSHOT.jar"

status:
	@echo "📊 Service Status:"
	@echo ""
	@echo "Database:"
	@$(DOCKER_COMPOSE) ps db || echo "  ❌ Not running"
	@echo ""
	@echo "Server:"
	@ps aux | grep "server.*SNAPSHOT.jar" | grep -v grep || echo "  ❌ Not running"
	@echo ""
	@echo "Client:"
	@ps aux | grep "com.ltm.game.client.ClientApp" | grep -v grep || echo "  ❌ Not running"

