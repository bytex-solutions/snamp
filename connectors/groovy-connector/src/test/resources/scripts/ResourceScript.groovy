package scripts

def container = 0

attribute {
    name "CustomAttribute"
    type INT64
    description "Test attribute"
    get { container }
    set {value -> container = value}
}

operation {
    name "CustomOperation"
    description "Test operation"
    parameter "x", FLOAT64
    parameter "y", FLOAT64
    returns FLOAT64
    implementation {x, y -> x + y}
}