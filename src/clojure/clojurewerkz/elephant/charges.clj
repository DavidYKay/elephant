(ns clojurewerkz.elephant.charges
  "Operations on charges"
  (:refer-clojure :exclude [list])
  (:require [clojurewerkz.elephant.conversion :as cnv]
            [clojurewerkz.elephant.util :refer (api-key->request-options)]
            [clojure.walk :as wlk])
  (:import [com.stripe.model Charge]
           [clojure.lang IPersistentMap]))

;;
;; API
;;

(defn list
  ([m]
     (cnv/charge-coll->seq (Charge/list (wlk/stringify-keys m))))
  ([^String api-key m]
     (cnv/charge-coll->seq (Charge/list (wlk/stringify-keys m) (api-key->request-options api-key)))))

(defn create
  ([m]
     (cnv/charge->map (Charge/create (wlk/stringify-keys m))))
  ([^String api-key m]
     (cnv/charge->map (Charge/create (wlk/stringify-keys m) api-key))))

(defn retrieve
  ([^String id]
     (cnv/charge->map (Charge/retrieve id)))
  ([^String id ^String key]
     (cnv/charge->map (Charge/retrieve id key))))

(defn refund
  ([^IPersistentMap charge]
     (refund charge {}))
  ([^IPersistentMap charge ^IPersistentMap opts]
     (if-let [o (:__origin__ charge)]
       (cnv/charge->map (.refund o opts))
       (throw (IllegalArgumentException.
               "charges/refund only accepts maps returned by charges/create and charges/retrieve")))))

(defn capture
  [^IPersistentMap m]
  (if-let [o (:__origin__ m)]
    (cnv/charge->map (.capture o))
    (throw (IllegalArgumentException.
            "charges/capture only accepts maps returned by charges/create and charges/retrieve"))))
