package scripts

def container = 0

attribute {
    name "CustomAttribute"
    type INT64
    description "Test attribute"
    get {return container}
    set {value -> container = value}
}