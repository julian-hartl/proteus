package binding

abstract class BoundNode {
    abstract val kind: BoundNodeKind
}

enum class BoundNodeKind {
    UnaryExpression,
    LiteralExpression,
    BinaryExpression
}