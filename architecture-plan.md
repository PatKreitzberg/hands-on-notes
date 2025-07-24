# Note-Taking Application Architecture Plan

## Overview

This document outlines the architecture for a handwritten note-taking application optimized for Onyx e-ink tablets. The architecture prioritizes performance for real-time drawing while maintaining clean separation of concerns and extensibility.

## Core Architecture Principles

1. **Performance First**: Direct calls for critical paths (drawing, touch handling)
2. **Modular Design**: Feature modules that can be added/removed without affecting core
3. **Clean Separation**: Clear boundaries between UI, business logic, and data layers
4. **Memory Efficient**: Object pooling and careful memory management for smooth drawing
5. **LLM-Friendly**: Well-organized packages that minimize context needed for changes

## High-Level Architecture

```mermaid
graph TB
    subgraph "UI Layer"
        HomeView[Home View]
        EditorView[Editor View]
        Toolbar[Toolbar Component]
        DrawingCanvas[Drawing Canvas/SurfaceView]
    end
    
    subgraph "Core Drawing System"
        DrawingActivity[Drawing Activity]
        DrawingCore[Drawing Core]
        TouchProcessor[Touch Processor]
        CanvasRenderer[Canvas Renderer]
        ShapeRepository[Shape Repository]
        CommandExecutor[Command Executor]
    end
    
    subgraph "Feature Modules"
        DrawingModule[Drawing Module]
        SelectionModule[Selection Module]
        LayersModule[Layers Module]
        UndoRedoModule[Undo/Redo Module]
        TemplateModule[Template Module]
        ExportModule[Export Module]
        MLKitModule[Text Recognition]
        SyncModule[Google Drive Sync]
    end
    
    subgraph "Data Layer"
        NoteRepository[Note Repository]
        RoomDatabase[(Room Database)]
        MemoryCache[Memory Cache]
        ShapeSerializer[Shape Serializer]
        FileManager[File Manager]
    end
    
    subgraph "Platform Integration"
        OnyxSDK[Onyx SDK Integration]
        RefreshManager[E-ink Refresh Manager]
        GestureHandler[Gesture Handler]
    end
    
    %% UI Layer connections
    HomeView --> NoteRepository
    EditorView --> DrawingActivity
    Toolbar --> DrawingCore
    DrawingCanvas --> TouchProcessor
    
    %% Core system connections
    DrawingActivity --> DrawingCore
    DrawingActivity --> TouchProcessor
    DrawingActivity --> OnyxSDK
    TouchProcessor --> DrawingCore
    DrawingCore --> CanvasRenderer
    DrawingCore --> ShapeRepository
    DrawingCore --> CommandExecutor
    CanvasRenderer --> RefreshManager
    
    %% Feature module connections
    DrawingModule --> DrawingCore
    SelectionModule --> DrawingCore
    SelectionModule --> CanvasRenderer
    LayersModule --> ShapeRepository
    LayersModule --> CanvasRenderer
    UndoRedoModule --> CommandExecutor
    TemplateModule --> CanvasRenderer
    ExportModule --> ShapeRepository
    ExportModule --> FileManager
    MLKitModule --> ShapeRepository
    SyncModule --> NoteRepository
    
    %% Data layer connections
    DrawingCore -.-> NoteRepository
    NoteRepository --> RoomDatabase
    NoteRepository --> MemoryCache
    NoteRepository --> ShapeSerializer
    ShapeRepository --> MemoryCache
    
    %% Platform connections
    TouchProcessor --> OnyxSDK
    TouchProcessor --> GestureHandler
    CanvasRenderer --> OnyxSDK
```

## Drawing Operation Flow

```mermaid
graph LR
    subgraph "Touch Input"
        Touch[Touch Event] --> TouchProcessor
        TouchProcessor --> ShapeFactory[Shape Factory]
    end
    
    subgraph "Command Creation"
        ShapeFactory --> DrawCommand[Draw Command]
        DrawCommand --> CommandExecutor
        CommandExecutor --> UndoStack[Undo Stack]
    end
    
    subgraph "Shape Management"
        DrawCommand --> ShapeRepository
        ShapeRepository --> MemoryCache
        ShapeRepository --> ActiveShapes[Active Shapes List]
    end
    
    subgraph "Rendering"
        DrawCommand --> CanvasRenderer
        CanvasRenderer --> DirtyRegion[Dirty Region Tracker]
        DirtyRegion --> PartialRefresh[Partial Refresh]
        CanvasRenderer --> Bitmap
        Bitmap --> SurfaceView
    end
    
    subgraph "Async Operations"
        ShapeRepository -.-> AutoSave[Auto Save]
        AutoSave -.-> Database
        ShapeRepository -.-> FeatureListeners[Feature Listeners]
    end
```

## Class Interactions

```mermaid
graph TD
    subgraph "Core Classes"
        DrawingCore[DrawingCore<br/>- Coordinates operations<br/>- Manages features]
        TouchProcessor[TouchProcessor<br/>- Handles touch events<br/>- Creates shapes]
        CanvasRenderer[CanvasRenderer<br/>- Renders to bitmap<br/>- Manages render stack]
        ShapeRepo[ShapeRepository<br/>- In-memory shape storage<br/>- Layer management]
        CmdExecutor[CommandExecutor<br/>- Executes commands<br/>- Manages undo/redo]
    end
    
    subgraph "Commands"
        DrawCmd[DrawShapeCommand]
        EraseCmd[EraseShapeCommand]
        MoveCmd[MoveShapeCommand]
        LayerCmd[LayerCommand]
    end
    
    subgraph "Renderers"
        TemplateRenderer[Template Renderer<br/>Priority: 0]
        ShapeRenderer[Shape Renderer<br/>Priority: 1]
        SelectionRenderer[Selection Renderer<br/>Priority: 2]
    end
    
    TouchProcessor -->|creates| DrawCmd
    DrawCmd -->|executed by| CmdExecutor
    CmdExecutor -->|modifies| ShapeRepo
    CmdExecutor -->|triggers| CanvasRenderer
    
    CanvasRenderer -->|uses| TemplateRenderer
    CanvasRenderer -->|uses| ShapeRenderer
    CanvasRenderer -->|uses| SelectionRenderer
    
    ShapeRepo -->|provides shapes| ShapeRenderer
    
    DrawingCore -->|coordinates| TouchProcessor
    DrawingCore -->|coordinates| CanvasRenderer
    DrawingCore -->|coordinates| ShapeRepo
    DrawingCore -->|coordinates| CmdExecutor
```

## Database Schema

```mermaid
graph TD
    subgraph "Database Entities"
        Folder[Folder<br/>- id: String<br/>- name: String<br/>- parentId: String?<br/>- lastModified: Long]
        
        Notebook[Notebook<br/>- id: String<br/>- name: String<br/>- folderId: String<br/>- coverImagePath: String?<br/>- lastModified: Long]
        
        Note[Note<br/>- id: String<br/>- title: String<br/>- notebookId: String?<br/>- folderId: String?<br/>- pageSettings: PageSettings<br/>- lastModified: Long]
        
        StoredShape[StoredShape<br/>- id: String<br/>- noteId: String<br/>- pageNumber: Int<br/>- layerId: String<br/>- shapeData: ByteArray<br/>- boundingBox: RectF<br/>- timestamp: Long]
        
        Layer[Layer<br/>- id: String<br/>- noteId: String<br/>- name: String<br/>- position: Int<br/>- isVisible: Boolean<br/>- opacity: Float]
    end
    
    Folder -->|contains| Folder
    Folder -->|contains| Notebook
    Folder -->|contains| Note
    Notebook -->|contains| Note
    Note -->|has many| StoredShape
    Note -->|has many| Layer
    Layer -->|contains| StoredShape
```

## Package Structure

```
com.wyldsoft.notes/
├── core/                     # Core business logic
│   ├── drawing/             # Hot path - drawing operations
│   │   ├── DrawingCore.kt
│   │   ├── TouchProcessor.kt
│   │   └── ShapeFactory.kt
│   ├── rendering/           # Rendering system
│   │   ├── CanvasRenderer.kt
│   │   ├── RenderStack.kt
│   │   └── DirtyRegionTracker.kt
│   ├── commands/            # Command pattern
│   │   ├── CommandExecutor.kt
│   │   ├── DrawCommand.kt
│   │   └── CommandPool.kt
│   └── repository/          # In-memory repositories
│       ├── ShapeRepository.kt
│       └── LayerManager.kt
│
├── features/                # Feature modules
│   ├── essential/          # Always loaded
│   │   ├── drawing/
│   │   ├── eraser/
│   │   └── undo/
│   └── optional/           # Loaded on demand
│       ├── selection/
│       ├── layers/
│       ├── templates/
│       ├── export/
│       ├── sync/
│       └── mlkit/
│
├── data/                    # Data persistence
│   ├── database/           # Room entities and DAOs
│   │   ├── entities/
│   │   ├── dao/
│   │   └── AppDatabase.kt
│   ├── repository/         # Repository implementations
│   ├── serialization/      # Shape serialization
│   └── cache/             # Memory caching
│
├── ui/                      # UI Layer
│   ├── home/               # Home screen
│   ├── editor/             # Editor screen
│   ├── components/         # Shared components
│   └── theme/             # Theming
│
├── platform/               # Platform-specific code
│   ├── sdk/               # SDK integration
│   │   ├── DrawingActivity.kt
│   │   └── onyx/
│   ├── gestures/          # Gesture handling
│   └── refresh/           # E-ink refresh management
│
└── utils/                  # Utilities
    ├── extensions/
    ├── performance/
    └── logging/
```

## Interface Definitions

### Core System Interfaces

```kotlin
// Drawing system core interfaces
interface DrawingCore {
    fun addShape(shape: Shape)
    fun removeShape(shapeId: String)
    fun executeCommand(command: DrawCommand)
    fun registerFeature(feature: FeatureModule)
    fun getShapeRepository(): ShapeRepository
    fun getRenderer(): CanvasRenderer
}

interface TouchProcessor {
    fun processTouchPoints(points: TouchPointList)
    fun setPenProfile(profile: PenProfile)
    fun setMode(mode: TouchMode)
}

interface CanvasRenderer {
    fun renderShape(shape: Shape)
    fun refreshRegion(region: RectF)
    fun fullRefresh()
    fun addRenderer(renderer: ObjectRenderer, priority: Int)
    fun removeRenderer(renderer: ObjectRenderer)
}

interface ShapeRepository {
    fun addShape(shape: Shape)
    fun removeShape(shapeId: String)
    fun getShapesInRegion(region: RectF): List<Shape>
    fun getAllShapes(): List<Shape>
    fun clear()
}
```

### Command Pattern Interfaces

```kotlin
interface Command {
    fun execute()
    fun undo()
    fun canMergeWith(other: Command): Boolean = false
}

interface CommandExecutor {
    fun execute(command: Command)
    fun undo(): Boolean
    fun redo(): Boolean
    fun canUndo(): Boolean
    fun canRedo(): Boolean
    fun clearHistory()
}
```

### Feature Module System Interfaces

```kotlin
interface FeatureModule {
    val id: String
    fun initialize(context: FeatureContext)
    fun cleanup()
    fun isEnabled(): Boolean
}

interface FeatureContext {
    val drawingCore: DrawingCore
    val gestureHandler: GestureHandler
    val toolbar: ToolbarManager
    val eventChannel: EventChannel
}

interface GestureHandler {
    fun addGestureRecognizer(recognizer: GestureRecognizer)
    fun removeGestureRecognizer(recognizer: GestureRecognizer)
    fun setExclusiveMode(recognizer: GestureRecognizer?)
}
```

### Rendering System Interfaces

```kotlin
interface ObjectRenderer {
    val priority: Int
    fun render(canvas: Canvas)
    fun shouldRender(): Boolean = true
    fun getDirtyRegion(): RectF?
}

interface RenderStack {
    fun addRenderer(renderer: ObjectRenderer)
    fun removeRenderer(renderer: ObjectRenderer)
    fun renderAll(canvas: Canvas)
    fun invalidateRegion(region: RectF)
}

interface RefreshManager {
    fun requestPartialRefresh(region: RectF)
    fun requestFullRefresh()
    fun setRefreshMode(mode: RefreshMode)
}
```

### Data Layer Interfaces

```kotlin
interface NoteRepository {
    suspend fun getNote(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun getNotesByFolder(folderId: String): List<Note>
    suspend fun getNotesByNotebook(notebookId: String): List<Note>
}

interface ShapeSerializer {
    fun serialize(shape: Shape): ByteArray
    fun deserialize(data: ByteArray): Shape
    fun serializeBatch(shapes: List<Shape>): ByteArray
    fun deserializeBatch(data: ByteArray): List<Shape>
}

interface SyncService {
    suspend fun syncNote(noteId: String)
    suspend fun syncAll()
    fun getSyncStatus(noteId: String): SyncStatus
    fun addSyncListener(listener: SyncListener)
}
```

### Platform/SDK Interfaces

```kotlin
interface DeviceSDK {
    fun initialize(context: Context)
    fun createTouchHelper(): Any
    fun setupRefreshMode(view: View)
    fun requestPartialRefresh(view: View, region: RectF)
    fun getDeviceCapabilities(): DeviceCapabilities
}

interface ShapeFactory {
    fun createShape(
        points: TouchPointList,
        penProfile: PenProfile,
        pressure: Float
    ): Shape
}

interface TouchEventProcessor {
    fun onTouchEvent(event: MotionEvent): Boolean
    fun onTouchPoints(points: TouchPointList)
    fun setTouchListener(listener: TouchListener)
}
```

### UI Layer Interfaces

```kotlin
interface ToolbarManager {
    fun addAction(action: ToolbarAction)
    fun removeAction(actionId: String)
    fun setSelectedPenProfile(profile: PenProfile)
    fun showOptions(options: List<ToolbarOption>)
}

interface DrawingView {
    fun getCanvas(): Canvas
    fun invalidate(region: RectF)
    fun setOnDrawListener(listener: OnDrawListener)
}

interface EditorScreen {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun navigateToHome()
}
```

### Event System Interfaces

```kotlin
// For non-critical async operations only
interface EventChannel {
    suspend fun send(event: AppEvent)
    fun tryOffer(event: AppEvent): Boolean
    fun asFlow(): Flow<AppEvent>
}

interface EventListener<T : AppEvent> {
    fun onEvent(event: T)
}
```

### Memory Management Interfaces

```kotlin
interface ObjectPool<T> {
    fun obtain(): T
    fun recycle(obj: T)
    fun clear()
    fun getPoolSize(): Int
}

interface MemoryCache<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
    fun remove(key: K)
    fun clear()
    fun evictAll()
}
```

### Feature-Specific Interfaces

```kotlin
// Selection feature
interface SelectionManager {
    fun startSelection(startPoint: PointF)
    fun updateSelection(currentPoint: PointF)
    fun completeSelection(): Selection?
    fun clearSelection()
    fun getSelectedShapes(): List<Shape>
}

// Layer feature
interface LayerManager {
    fun createLayer(name: String): Layer
    fun deleteLayer(layerId: String)
    fun setActiveLayer(layerId: String)
    fun reorderLayers(layerIds: List<String>)
    fun getLayersForNote(noteId: String): List<Layer>
}

// Export feature
interface ExportService {
    suspend fun exportToPDF(note: Note, outputPath: String)
    suspend fun exportToPNG(note: Note, outputPath: String)
    suspend fun exportToSVG(note: Note, outputPath: String)
}
```

## Key Design Patterns

### 1. Command Pattern for Undo/Redo
```kotlin
interface DrawCommand {
    fun execute()
    fun undo()
    fun canMergeWith(other: DrawCommand): Boolean = false
}

class DrawShapeCommand(
    private val shape: Shape,
    private val repository: ShapeRepository,
    private val renderer: CanvasRenderer
) : DrawCommand {
    override fun execute() {
        repository.addShape(shape)
        renderer.requestRender()
    }
    
    override fun undo() {
        repository.removeShape(shape.id)
        renderer.requestRender()
    }
}
```

### 2. Feature Module Pattern
```kotlin
class SelectionModule : FeatureModule {
    override val id = "selection"
    
    override fun initialize(context: FeatureContext) {
        context.gestureHandler.addGestureRecognizer(
            LassoGestureRecognizer { path ->
                handleLassoComplete(path)
            }
        )
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override fun isEnabled() = true
}
```

### 3. Render Stack Pattern
```kotlin
abstract class ObjectRenderer(val priority: Int) {
    abstract fun render(canvas: Canvas)
    open fun shouldRender(): Boolean = true
}

class RenderStackManager {
    private val renderers = sortedSetOf<ObjectRenderer>(
        compareBy { it.priority }
    )
    
    fun render(canvas: Canvas) {
        renderers.forEach { 
            if (it.shouldRender()) {
                it.render(canvas)
            }
        }
    }
}
```

## Performance Optimizations

1. **Direct Method Calls**: No event bus for critical paths
2. **Object Pooling**: Reuse shapes and commands to reduce GC
3. **Dirty Region Tracking**: Only refresh changed areas
4. **Batch Operations**: Group database writes
5. **Async Operations**: Non-critical features run in background
6. **Memory Cache**: LRU cache for frequently accessed data

## Feature Implementation Guidelines

### Adding a New Feature

1. Create a feature module in `features/optional/`
2. Implement the `FeatureModule` interface
3. Register gesture recognizers or event listeners
4. Use `DrawCommand` for any undoable operations
5. Add renderers to the render stack if needed
6. Keep feature-specific code isolated in the module

### Example: Adding Lasso Selection

```kotlin
class SelectionModule : FeatureModule {
    override val id = "selection"
    private lateinit var selectionManager: SelectionManager
    private lateinit var selectionRenderer: SelectionRenderer
    
    override fun initialize(context: FeatureContext) {
        selectionManager = SelectionManagerImpl(context.drawingCore.getShapeRepository())
        selectionRenderer = SelectionRenderer()
        
        // Register gesture
        context.gestureHandler.addGestureRecognizer(
            LassoGestureRecognizer { path ->
                val selected = findShapesInPath(path)
                selectionManager.setSelection(selected)
                context.drawingCore.getRenderer().addRenderer(selectionRenderer, priority = 100)
            }
        )
        
        // Register toolbar action
        context.toolbar.addAction(
            SelectionAction { 
                context.drawingCore.getRenderer().setMode(TouchMode.SELECTION)
            }
        )
    }
    
    override fun cleanup() {
        context.drawingCore.getRenderer().removeRenderer(selectionRenderer)
    }
    
    override fun isEnabled() = true
}
```

## Future Considerations

1. **Multi-device Support**: Abstract SDK layer allows easy addition of other stylus devices
2. **Collaboration**: Architecture supports adding real-time sync later
3. **Plugin System**: Feature modules can be loaded dynamically
4. **Performance Monitoring**: Add metrics to track frame times
5. **Testing**: Clean interfaces make unit testing straightforward

## Summary

This architecture provides:
- **High performance** for drawing operations
- **Clean separation** of concerns
- **Easy feature addition** through modules
- **LLM-friendly** organization
- **Scalable** data management
- **Platform agnostic** core logic