# Ubuntu: sudo apt install libhiredis-dev
# Mac: brew install hiredis


if (!require("redux")) install.packages("redux", repos = "http://cran.us.r-project.org")




r <- redux::hiredis()

transform <- function(x) {
  message(format(Sys.time(), "%Y-%m-%d %H:%M:%OS3"),
          ": got message: ",
          x$type,
          " ", 
          x$pattern,
          " ", 
          x$channel,
          " ", 
          x$value)
  x$value
}

# r$subscribe("Royal_University_Hospital_patient")

res <- r$subscribe("Royal_University_Hospital_patient",
                   transform = transform,
                   terminate = function(x) identical(x, "goodbye"),
                   n = 100)