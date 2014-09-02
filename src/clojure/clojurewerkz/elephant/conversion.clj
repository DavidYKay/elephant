(ns clojurewerkz.elephant.conversion
  "Internal Java object => persistent Clojure data structure conversion functions.
   Not supposed to be used directly, not a part of the public Elephant API."
  (:import [clojure.lang IPersistentMap]
           java.util.List
           [com.stripe.model StripeCollection StripeColllectionAPIResource
            Account Balance BalanceTransaction Card Charge Dispute Fee Money
            Refund]))


;;
;; API
;;

(defn ^IPersistentMap account->map
  [^Account acc]
  {:id    (.getId acc)
   :email (.getEmail acc)
   :currencies-supported (vec (.getCurrenciesSupported acc))
   :charge-enabled       (.getChargeEnabled acc)
   :transfer-enabled     (.getTransferEnabled acc)
   :details-submitted    (.getDetailsSubmitted acc)
   :statement-descriptor (.getStatementDescriptor acc)
   :default-currency     (.getDefaultCurrency acc)
   :country              (.getCountry acc)
   :timezone             (.getTimezone acc)
   :display-name         (.getDisplayName acc)
   :__origin__           acc})

(defn ^IPersistentMap money->map
  [^Money m]
  {:amount   (.getAmount m)
   :currency (.getCurrency m)
   :__origin__ m})

(defn ^IPersistentMap balance->map
  [^Balance b]
  {:available  (map money->map (.getAvailable b))
   :pending    (map money->map (.getPending b))
   :live-mode? (.getLivemode b)
   :__origin__ b})

(defn ^IPersistentMap card->map
  [^Card c]
  {:id               (.getId c)
   :expiration-month (.getExpMonth c)
   :expiration-year  (.getExpYear c)
   :last-4-digits    (.getLast4 c)
   :country          (.getCountry c)
   :type             (.getType c)
   :name             (.getName c)
   :customer         (.getCustomer c)
   :recipient        (.getRecipient c)
   :address {:line1    (.getAddressLine1 c)
             :line2    (.getAddressLine2 c)
             :zip      (.getAddressZip c)
             :city     (.getAddressCity c)
             :state    (.getAddressState c)
             :country  (.getAddressCountry c)
             :zip-check   (.getAddressZipCheck c)
             :line1-check (.getAddressLine1Check c)}
   :cvc-check   (.getCvcCheck c)
   :fingerprint (.getFingerprint c)
   :brand       (.getBrand c)
   :funding     (.getFunding c)
   :__origin__  c})

(defn ^IPersistentMap fee->map
  [^Fee fe]
  {:type        (.getType fe)
   :application (.getApplication fe)
   :amount      (.getAmount fe)
   :description (.getDescription fe)
   :currency    (.getCurrency fe)
   :__origin__ fe})

(defn ^List fees->seq
  [^List xs]
  (map fee->map xs))

(defn ^IPersistentMap balance-tx->map
  [^BalanceTransaction tx]
  {:id       (.getId tx)
   :source   (.getSource tx)
   :amount   (.getAmount tx)
   :currency (.getCurrency tx)
   :net      (.getNet tx)
   :type     (.getType tx)
   ;; TODO: convert to UTC date with clj-time
   :created  (.getCreated tx)
   :available-on (.getAvailableOn tx)
   :status       (.getStatus tx)
   :fee          (.getFee tx)
   :fee-details  (fees->seq (.getFeeDetails tx))
   :description  (.getDescription tx)
   :__origin__   tx})

(defn ^List balance-txs->seq
  [^List xs]
  (doall (map balance-tx->map xs)))

(defn balance-tx-coll->seq
  [^StripeCollection txs]
  ;; TODO: pagination
  (map balance-tx->map (.getData txs)))

(defn ^IPersistentMap refund->map
  [^Refund r]
  {:id       (.getId r)
   :amount   (.getAmount r)
   ;; TODO: convert to UTC date with clj-time
   :created  (.getCreated r)
   :currency (.getCurrency r)
   :balance-transactions (.getBalanceTransaction r)
   :charge   (.getCharge r)
   :metadata (into {} (.getMetadata r))
   :__origin__ r})

(defn refunds-coll->seq
  [^StripeColllectionAPIResource xs]
  (map refund->map (.getData xs)))

(defn ^IPersistentMap dispute->map
  [^Dispute d]
  {:charge   (.getCharge d)
   :amount   (.getAmount d)
   :status   (.getStatus d)
   :currency (.getCurrency d)
   ;; TODO: convert to UTC date with clj-time
   :created  (.getCreated d)
   :live-mode? (.getLivemode d)
   :evidence   (.getEvidence d)
   ;; TODO: convert to UTC date with clj-time
   :evidence-due-by (.getEvidenceDueBy d)
   :reason          (.getReason d)
   :balance-transactions (balance-txs->seq (.getBalanceTransactions d))
   :metadata             (into {} (.getMetadata d))
   :__origin__           d})

(defn ^IPersistentMap charge->map
  [^Charge c]
  {:id         (.getId c)
   :currency   (.getCurrency c)
   :amount     (.getAmount c)
   :created    (.getCreated c)
   :live-mode? (.getLivemode c)
   :paid?      (.getPaid c)
   :refunded?  (.getRefunded c)
   :refunds    (doall (map refund->map (if-let [^StripeColllectionAPIResource xs (.getRefunds c)]
                                         (.getData xs)
                                         [])))
   :captured?  (.getCaptured c)
   :dispute    (when-let [d (.getDispute c)]
                 (dispute->map d))
   :card       (card->map (.getCard c))
   :description           (.getDescription c)
   :statement-description (.getStatementDescription c)
   :invoice               (.getInvoice c)
   :customer              (.getCustomer c)
   :failure-message       (.getFailureMessage c)
   :failure-code          (.getFailureCode c)
   :__origin__            c})