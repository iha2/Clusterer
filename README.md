
#Clusterer

**Clusterer** is a repository containing several implementations of clustering
algorithms. Each implementations will contain the data sets that will have
served as test and well as anything else that will faciliate a greater
description of the each algorithms.


Akka: 
![Akka Framework][logo]

[logo]:
the http://akka.io/resources/images/akka_full_color.svg
"Akka"

All implementations are done in **Scala** and implementated using the **Akka** Framework.
The Akka framework uses the **Actor Sytem** to implement concurrent and
asynchronous systems. The clustering algorithms are computed as concurrently as
possible.

Spray Can:
![Spray Fraemwork][logo]

[logo]:
http://spray.io/img/logo-large.png
"Spray"

**Spray** is http client fraework that sits on top of Akka to send and recieve http
requests. In Clusterer, we use Spray to send data to clients to render the data
computed by the algorithms to a web browser.


###Hirarchaelcal Clustering Algorithm Implementation

A hirarchaecal clustering algorithm is a machine learning algorithm that
compares vectors of data using a **distance metric** in order to discover
relationships bwteen data based on their similarity. The vectors serve as
numerical information, such as weight and height, that help characterize the
objects in which we would like to find relationships. We then use distance
metrics (Eucledian distance, Pearson correlation score e.t.c) to characterize
these relationships.





